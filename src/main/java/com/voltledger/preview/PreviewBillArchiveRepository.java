package com.voltledger.preview;

import com.voltledger.billing.ElectricityBill;
import com.voltledger.data.BillArchiveRepository;
import com.voltledger.data.InvoiceSummary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** Preview is intentionally in-memory; Firebase must be enabled for permanent records. */
@Repository
@Profile("preview")
public class PreviewBillArchiveRepository implements BillArchiveRepository {
    private final Map<String, ElectricityBill> bills = new HashMap<>();
    private final Map<String, Customer> consumers = new HashMap<>();
    private final Map<String, String> meterIndex = new HashMap<>();
    private static final Comparator<InvoiceSummary> NEWEST = Comparator
        .comparing(InvoiceSummary::generatedOn).thenComparing(InvoiceSummary::invoiceNumber).reversed();

    private record Customer(String id, String name, String address, String meter, String owner) {
        Customer withOwner(String username) { return new Customer(id, name, address, meter, username); }
        Customer withDetails(String fullName, String location) {
            return new Customer(id, fullName, location, meter, owner);
        }
    }

    @Override public synchronized void createConsumer(String id, String name, String address,
            String meterNumber, String ownerUsername) {
        if (consumers.containsKey(id) || meterIndex.containsKey(meterNumber))
            throw new DuplicateKeyException("Consumer or meter already exists");
        consumers.put(id, new Customer(id, name, address, meterNumber, ownerUsername));
        meterIndex.put(meterNumber, id);
    }

    @Override public synchronized void save(ElectricityBill bill, String ownerUsername) {
        var customer = bill.consumer();
        String id = customer.getConsumerId();
        String meter = customer.getMeterNumber();
        if (bills.containsKey(bill.invoiceNumber()))
            throw new DuplicateKeyException("Invoice number already exists");
        Customer existing = consumers.get(id);
        if (existing == null) {
            if (meterIndex.containsKey(meter)) throw new DuplicateKeyException("Meter already assigned");
            consumers.put(id, new Customer(id,customer.getName(),customer.getAddress(),meter,ownerUsername));
            meterIndex.put(meter,id);
        } else {
            if (!existing.owner.equals(ownerUsername) || !existing.meter.equals(meter))
                throw new IllegalArgumentException("Consumer belongs to a different account or meter");
            consumers.put(id, existing.withDetails(customer.getName(),customer.getAddress()));
        }
        bills.put(bill.invoiceNumber(), bill);
    }

    private InvoiceSummary summary(ElectricityBill bill) {
        return new InvoiceSummary(bill.invoiceNumber(), bill.generatedOn(),
            bill.consumer().getConsumerId(), bill.consumer().getName(), bill.consumer().getMeterNumber(),
            bill.tariffName(), bill.consumedUnits(), bill.total());
    }

    @Override public synchronized List<InvoiceSummary> allInvoices() {
        return bills.values().stream().map(this::summary).sorted(NEWEST).toList();
    }

    @Override public synchronized List<InvoiceSummary> invoicesFor(String username) {
        return searchInvoices(username, "", "", null, null);
    }

    @Override public synchronized List<InvoiceSummary> searchInvoices(String ownerUsername, String search,
            String tariff, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) return List.of();
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        List<InvoiceSummary> result = new ArrayList<>();
        for (ElectricityBill bill : bills.values()) {
            InvoiceSummary s = summary(bill);
            Customer customer = consumers.get(s.consumerId());
            if (ownerUsername != null && (customer == null || !ownerUsername.equals(customer.owner))) continue;
            if (!query.isEmpty() && !(s.invoiceNumber().toLowerCase(Locale.ROOT).contains(query)
                || s.consumerId().toLowerCase(Locale.ROOT).contains(query)
                || s.consumerName().toLowerCase(Locale.ROOT).contains(query)
                || s.meterNumber().toLowerCase(Locale.ROOT).contains(query))) continue;
            if (tariff != null && ("Residential".equalsIgnoreCase(tariff) || "Commercial".equalsIgnoreCase(tariff))
                && !tariff.equalsIgnoreCase(s.tariffName())) continue;
            if (from != null && s.generatedOn().isBefore(from)) continue;
            if (to != null && s.generatedOn().isAfter(to)) continue;
            result.add(s);
        }
        return result.stream().sorted(NEWEST).toList();
    }

    @Override public synchronized List<ConsumerAssignment> allConsumers() {
        return consumers.values().stream()
            .map(c -> new ConsumerAssignment(c.id, c.name, c.meter, c.owner))
            .sorted(Comparator.comparing(ConsumerAssignment::consumerId)).toList();
    }

    @Override public synchronized int assignOwner(String id, String username) {
        Customer c = consumers.get(id);
        if (c == null) return 0;
        consumers.put(id, c.withOwner(username));
        return 1;
    }

    @Override public synchronized int countConsumers() { return consumers.size(); }

    @Override public synchronized Optional<ElectricityBill> invoice(String number, String ownerUsername) {
        ElectricityBill bill = bills.get(number);
        if (bill == null) return Optional.empty();
        Customer customer = consumers.get(bill.consumer().getConsumerId());
        if (ownerUsername != null && (customer == null || !ownerUsername.equals(customer.owner)))
            return Optional.empty();
        return Optional.of(bill);
    }
}
