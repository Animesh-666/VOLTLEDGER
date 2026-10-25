package com.voltledger.firebase;

import com.voltledger.billing.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FirestoreInvoiceCodecTest {
    @Test void roundTripPreservesExactCurrencyAndChargeLines() {
        var bill = new BillingService().calculate(new BillRequest(
            new Consumer("C100", "Demo", "Kolkata", "M100"),
            TariffType.RESIDENTIAL, new BigDecimal("275")));
        var encoded = FirestoreInvoiceCodec.encode(bill);
        var decoded = FirestoreInvoiceCodec.decode(encoded);
        assertEquals(bill.invoiceNumber(), decoded.invoiceNumber());
        assertEquals(0, bill.total().compareTo(decoded.total()));
        assertEquals(bill.lines(), decoded.lines());
        assertEquals("1758.75", encoded.get("total"));
    }
}
