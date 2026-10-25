package com.voltledger.web;

import com.voltledger.billing.ElectricityBill;
import com.voltledger.data.BillArchiveRepository;
import com.voltledger.invoice.InvoicePdfService;
import java.io.IOException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The same ownership policy applies to the PDF bytes as to the HTML invoice. */
@RestController
public class InvoiceExportController {
    private final BillArchiveRepository bills;
    private final InvoicePdfService pdf;
    public InvoiceExportController(BillArchiveRepository bills, InvoicePdfService pdf) {
        this.bills = bills; this.pdf = pdf;
    }

    @GetMapping(value = "/admin/bills/{number}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> adminPdf(@PathVariable String number) throws IOException {
        return download(bills.invoice(number, null).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping(value = "/account/bills/{number}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> accountPdf(@PathVariable String number, Authentication auth) throws IOException {
        return download(bills.invoice(number, auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    private ResponseEntity<byte[]> download(ElectricityBill bill) throws IOException {
        String safeId = bill.invoiceNumber().replaceAll("[^A-Za-z0-9-]", "_");
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header("X-Content-Type-Options", "nosniff")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=VOLTLEDGER-" + safeId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf.generate(bill));
    }
}
