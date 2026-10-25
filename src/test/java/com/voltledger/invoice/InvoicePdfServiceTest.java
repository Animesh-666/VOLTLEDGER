package com.voltledger.invoice;

import com.voltledger.billing.BillRequest;
import com.voltledger.billing.BillingService;
import com.voltledger.billing.Consumer;
import com.voltledger.billing.TariffType;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvoicePdfServiceTest {
    @Test void generatesReadablePdfFromJavaInvoice() throws Exception {
        var bill = new BillingService().calculate(new BillRequest(
            new Consumer("VL-C1001", "Demo Consumer", "Kolkata, West Bengal", "MTR-1001"),
            TariffType.RESIDENTIAL, new BigDecimal("275")));
        byte[] bytes = new InvoicePdfService().generate(bill);
        assertTrue(bytes.length > 1000);
        assertEquals("%PDF-", new String(bytes, 0, 5, StandardCharsets.US_ASCII));
        try (var doc = Loader.loadPDF(bytes)) {
            var content = new PDFTextStripper().getText(doc);
            assertTrue(content.contains("VOLTLEDGER"));
            assertTrue(content.contains("Demo Consumer"));
            assertTrue(content.contains(bill.invoiceNumber()));
            assertTrue(content.contains("1,758.75"));
        }
    }
}
