package com.voltledger.data;

import com.voltledger.billing.ElectricityBill;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Storage contract. All invoice ownership checks must be implemented by each adapter. */
public interface BillArchiveRepository {
    void save(ElectricityBill bill, String ownerUsername);
    List<InvoiceSummary> allInvoices();
    List<InvoiceSummary> searchInvoices(String ownerUsername, String search, String tariff, LocalDate from, LocalDate to);
    List<InvoiceSummary> invoicesFor(String username);
    void createConsumer(String consumerId, String name, String address, String meterNumber, String username);
    List<ConsumerAssignment> allConsumers();
    int assignOwner(String consumerId, String username);
    int countConsumers();
    Optional<ElectricityBill> invoice(String number, String ownerUsername);
    record ConsumerAssignment(String consumerId, String fullName, String meterNumber, String ownerUsername) { }
}
