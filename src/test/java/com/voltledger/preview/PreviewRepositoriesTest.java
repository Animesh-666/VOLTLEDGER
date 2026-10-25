package com.voltledger.preview;

import com.voltledger.billing.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import static org.junit.jupiter.api.Assertions.*;

class PreviewRepositoriesTest {
    @Test void consumerOwnershipBlocksOtherAccountsAndDuplicateMeters() {
        var accounts = new PreviewUserAccountRepository();
        accounts.create("firstuser", "First", "hash", "CONSUMER");
        accounts.create("seconduser", "Second", "hash", "CONSUMER");
        assertThrows(DuplicateKeyException.class, () ->
            accounts.create("firstuser", "Copy", "hash", "CONSUMER"));
        assertEquals(2, accounts.consumers().size());

        var archive = new PreviewBillArchiveRepository();
        archive.createConsumer("C101", "First", "Kolkata", "M101", "firstuser");
        assertThrows(DuplicateKeyException.class, () ->
            archive.createConsumer("C102", "Second", "Kolkata", "M101", "seconduser"));

        var bill = new BillingService().calculate(new BillRequest(
            new Consumer("C101", "First", "Kolkata", "M101"),
            TariffType.RESIDENTIAL, new BigDecimal("275")));
        archive.save(bill, "firstuser");
        assertEquals(1, archive.invoicesFor("firstuser").size());
        assertTrue(archive.invoicesFor("seconduser").isEmpty());
        assertTrue(archive.invoice(bill.invoiceNumber(), "seconduser").isEmpty());
        assertTrue(archive.invoice(bill.invoiceNumber(), "firstuser").isPresent());
        assertEquals(0, new BigDecimal("1758.75").compareTo(archive.invoice(bill.invoiceNumber(), "firstuser").orElseThrow().total()));
        assertEquals(1, archive.countConsumers());
        assertEquals(1, archive.assignOwner("C101", "seconduser"));
        assertTrue(archive.invoice(bill.invoiceNumber(), "firstuser").isEmpty());
        assertTrue(archive.invoice(bill.invoiceNumber(), "seconduser").isPresent());
    }
}
