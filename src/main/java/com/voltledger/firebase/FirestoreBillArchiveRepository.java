package com.voltledger.firebase;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import com.voltledger.billing.ElectricityBill;
import com.voltledger.data.BillArchiveRepository;
import com.voltledger.data.InvoiceSummary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** Firestore adapter; never trusts a client-supplied username for invoice access. */
@Repository
@Profile("firebase")
public class FirestoreBillArchiveRepository implements BillArchiveRepository {
    private final Firestore db;
    private static final Comparator<InvoiceSummary> NEWEST = Comparator
        .comparing(InvoiceSummary::generatedOn).thenComparing(InvoiceSummary::invoiceNumber).reversed();
    public FirestoreBillArchiveRepository(Firestore db) { this.db = db; }

    private DocumentReference consumer(String id) { return db.collection("consumers").document(id); }
    private DocumentReference meter(String number) {
        // Firestore document IDs cannot contain '/' and other reserved tokens.
        // SHA-256 hex is an unambiguous stable mapping for any permitted meter text.
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(number.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return db.collection("meter_index").document(java.util.HexFormat.of().formatHex(digest));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private DocumentReference invoiceRef(String id) { return db.collection("invoices").document(id); }

    private static Map<String,Object> consumerData(String id, String name, String address,
        String meterNumber, String owner) {
        return Map.of("consumerId", id,"fullName",name,"address",address,
            "meterNumber",meterNumber,"ownerUsername",owner);
    }
    private static String str(DocumentSnapshot snapshot, String key) { return snapshot.getString(key); }

    @Override public void createConsumer(String id, String name, String address,
        String meterNumber, String ownerUsername) {
        var c = consumer(id);
        var m = meter(meterNumber);
        FirestoreCalls.await(db.runTransaction(t -> {
            var old = FirestoreCalls.await(t.get(c));
            var oldMeter = FirestoreCalls.await(t.get(m));
            if (old.exists() || oldMeter.exists())
                throw new DuplicateKeyException("Consumer ID or meter number already registered");
            t.create(c, consumerData(id, name, address, meterNumber, ownerUsername));
            t.create(m, Map.of("consumerId", id, "meterNumber", meterNumber));
            return null;
        }));
    }

    @Override public void save(ElectricityBill b, String ownerUsername) {
        var customer = b.consumer();
        var c = consumer(customer.getConsumerId());
        var meterRef = meter(customer.getMeterNumber());
        var inv = invoiceRef(b.invoiceNumber());
        FirestoreCalls.await(db.runTransaction(t -> {
            // All reads first, all writes second (required by Firestore transaction semantics).
            var stored = FirestoreCalls.await(t.get(c));
            var reserved = FirestoreCalls.await(t.get(meterRef));
            var duplicateInvoice = FirestoreCalls.await(t.get(inv));
            if (duplicateInvoice.exists()) throw new DuplicateKeyException("Invoice already exists");
            if (!stored.exists()) {
                if (reserved.exists()) throw new DuplicateKeyException("Meter already registered");
                t.create(c, consumerData(customer.getConsumerId(),customer.getName(), customer.getAddress(),
                    customer.getMeterNumber(),ownerUsername));
                t.create(meterRef, Map.of("consumerId",customer.getConsumerId(),"meterNumber",customer.getMeterNumber()));
            } else {
                if (!customer.getMeterNumber().equals(str(stored,"meterNumber")) ||
                    !ownerUsername.equals(str(stored,"ownerUsername")))
                    throw new IllegalArgumentException("Consumer ID belongs to a different meter/account");
                if (reserved.exists() && !customer.getConsumerId().equals(str(reserved,"consumerId")))
                    throw new DuplicateKeyException("Meter already registered");
                t.update(c, Map.of("fullName",customer.getName(),"address",customer.getAddress()));
                if (!reserved.exists()) t.create(meterRef,
                    Map.of("consumerId",customer.getConsumerId(),"meterNumber",customer.getMeterNumber()));
            }
            t.create(inv, FirestoreInvoiceCodec.encode(b));
            return null;
        }));
    }

    private List<? extends DocumentSnapshot> allInvoiceDocuments() {
        return FirestoreCalls.await(db.collection("invoices").get()).getDocuments();
    }
    private Map<String, String> ownerByConsumer() {
        Map<String,String> ownership = new HashMap<>();
        for (DocumentSnapshot d : FirestoreCalls.await(db.collection("consumers").get()).getDocuments())
            ownership.put(d.getId(), str(d,"ownerUsername"));
        return ownership;
    }
    @Override public List<InvoiceSummary> allInvoices() {
        return allInvoiceDocuments().stream()
            .map(d -> FirestoreInvoiceCodec.summary(d.getData())).sorted(NEWEST).toList();
    }
    @Override public List<InvoiceSummary> invoicesFor(String owner) {
        return searchInvoices(owner, "", "", null, null);
    }
    @Override public List<InvoiceSummary> searchInvoices(String owner, String search, String tariff,
        LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) return List.of();
        // Intentional small-demo implementation. For public scale, use paginated, indexed queries.
        Map<String,String> ownership = owner == null ? Map.of() : ownerByConsumer();
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String category = tariff == null ? "" : tariff.trim();
        List<InvoiceSummary> matching = new ArrayList<>();
        for (DocumentSnapshot doc : allInvoiceDocuments()) {
            InvoiceSummary s = FirestoreInvoiceCodec.summary(doc.getData());
            if (owner != null && !owner.equals(ownership.get(s.consumerId()))) continue;
            if (!query.isEmpty() && !(s.invoiceNumber().toLowerCase(Locale.ROOT).contains(query) ||
                s.consumerId().toLowerCase(Locale.ROOT).contains(query) ||
                s.consumerName().toLowerCase(Locale.ROOT).contains(query) ||
                s.meterNumber().toLowerCase(Locale.ROOT).contains(query))) continue;
            if (("Residential".equalsIgnoreCase(category) || "Commercial".equalsIgnoreCase(category)) &&
                !category.equalsIgnoreCase(s.tariffName())) continue;
            if (from != null && s.generatedOn().isBefore(from)) continue;
            if (to != null && s.generatedOn().isAfter(to)) continue;
            matching.add(s);
        }
        return matching.stream().sorted(NEWEST).toList();
    }
    @Override public List<ConsumerAssignment> allConsumers() {
        return FirestoreCalls.await(db.collection("consumers").get()).getDocuments().stream()
            .map(d -> new ConsumerAssignment(d.getId(),str(d,"fullName"),str(d,"meterNumber"),str(d,"ownerUsername")))
            .sorted(Comparator.comparing(ConsumerAssignment::consumerId)).toList();
    }
    @Override public int assignOwner(String consumerId, String ownerUsername) {
        DocumentReference ref = consumer(consumerId);
        return FirestoreCalls.await(db.runTransaction(t -> {
            DocumentSnapshot d = FirestoreCalls.await(t.get(ref));
            if (!d.exists()) return 0;
            t.update(ref,"ownerUsername",ownerUsername);
            return 1;
        }));
    }
    @Override public int countConsumers() {
        return FirestoreCalls.await(db.collection("consumers").get()).size();
    }
    @Override public Optional<ElectricityBill> invoice(String number, String ownerUsername) {
        DocumentSnapshot doc = FirestoreCalls.await(invoiceRef(number).get());
        if (!doc.exists()) return Optional.empty();
        if (ownerUsername != null) {
            DocumentSnapshot assigned = FirestoreCalls.await(consumer(str(doc,"consumerId")).get());
            if (!assigned.exists() || !ownerUsername.equals(str(assigned,"ownerUsername")))
                return Optional.empty();
        }
        return Optional.of(FirestoreInvoiceCodec.decode(doc.getData()));
    }
}
