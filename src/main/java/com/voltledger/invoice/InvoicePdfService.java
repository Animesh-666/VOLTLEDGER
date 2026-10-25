package com.voltledger.invoice;

import com.voltledger.billing.BillLine;
import com.voltledger.billing.ElectricityBill;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

/** Generates a downloadable A4 PDF directly from an already saved Java bill. */
@Service
public class InvoicePdfService {
    private static final Color INK = new Color(24, 44, 39);
    private static final Color GREEN = new Color(30, 105, 75);
    private static final Color PALE = new Color(234, 247, 237);
    private static final Color MUTED = new Color(91, 111, 101);
    private static final Color WHITE = Color.WHITE;
    private static final float W = PDRectangle.A4.getWidth();
    private static final float LEFT = 44;
    private static final float RIGHT = W - LEFT;
    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] generate(ElectricityBill bill) throws IOException {
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);
            try (PDPageContentStream canvas = new PDPageContentStream(pdf, page)) {
                drawRect(canvas, 0, 736, W, 106, INK);
                text(canvas, "VOLTLEDGER", 24, BOLD, WHITE, LEFT, 794);
                text(canvas, "SMART ENERGY / BILLING", 9, REGULAR, new Color(197, 236, 202), LEFT, 776);
                rightText(canvas, "ELECTRICITY INVOICE", 10, BOLD, WHITE, RIGHT, 786);
                text(canvas, "ISSUED FOR AN ACADEMIC DEMONSTRATION", 8, REGULAR, MUTED, LEFT, 715);
                text(canvas, "INVOICE # " + bill.invoiceNumber(), 13, BOLD, INK, LEFT, 683);
                rightText(canvas, bill.generatedOn().format(DateTimeFormatter.ISO_LOCAL_DATE), 11, REGULAR, MUTED, RIGHT, 683);
                line(canvas, LEFT, 669, RIGHT, 669);

                text(canvas, "CONSUMER DETAILS", 9, BOLD, GREEN, LEFT, 645);
                text(canvas, "BILL SUMMARY", 9, BOLD, GREEN, 327, 645);
                text(canvas, fit(bill.consumer().getName(), BOLD, 13, 240), 13, BOLD, INK, LEFT, 625);
                float addressY = 606;
                for (String part : wrap(bill.consumer().getAddress(), REGULAR, 9, 235, 2)) {
                    text(canvas, part, 9, REGULAR, MUTED, LEFT, addressY);
                    addressY -= 13;
                }
                text(canvas, "Consumer: " + fit(bill.consumer().getConsumerId(), REGULAR, 9, 197), 9, REGULAR, MUTED, LEFT, 568);
                text(canvas, "Meter: " + fit(bill.consumer().getMeterNumber(), REGULAR, 9, 197), 9, REGULAR, MUTED, LEFT, 553);
                text(canvas, "Tariff: " + safe(bill.tariffName()), 10, REGULAR, INK, 327, 622);
                text(canvas, "Usage: " + bill.consumedUnits().toPlainString() + " kWh", 10, REGULAR, INK, 327, 603);
                text(canvas, "Generated: " + bill.generatedOn(), 10, REGULAR, INK, 327, 584);

                drawRect(canvas, LEFT, 473, RIGHT - LEFT, 63, PALE);
                text(canvas, "TOTAL AMOUNT PAYABLE", 9, BOLD, GREEN, LEFT + 15, 510);
                text(canvas, "INR " + money(bill.total()), 25, BOLD, INK, LEFT + 15, 485);
                text(canvas, "Detailed charges", 11, BOLD, INK, LEFT, 451);

                drawRect(canvas, LEFT, 419, RIGHT - LEFT, 22, PALE);
                text(canvas, "DESCRIPTION", 9, BOLD, GREEN, LEFT + 8, 426);
                rightText(canvas, "UNITS", 9, BOLD, GREEN, 338, 426);
                rightText(canvas, "RATE", 9, BOLD, GREEN, 407, 426);
                rightText(canvas, "AMOUNT", 9, BOLD, GREEN, RIGHT - 8, 426);
                float y = 401;
                for (BillLine charge : bill.lines()) {
                    if (y < 270) break; // Bounds protect the totals area from oversized input.
                    text(canvas, fit(charge.label(), REGULAR, 10, 210), 10, REGULAR, INK, LEFT + 8, y);
                    rightText(canvas, charge.units().toPlainString(), 9, REGULAR, MUTED, 338, y);
                    rightText(canvas, money(charge.rate()), 9, REGULAR, MUTED, 407, y);
                    rightText(canvas, money(charge.amount()), 10, BOLD, INK, RIGHT - 8, y);
                    line(canvas, LEFT, y - 11, RIGHT, y - 11);
                    y -= 27;
                }
                float sy = Math.min(y - 13, 293);
                totalsRow(canvas, "Energy charge", bill.energyCharge(), sy);
                totalsRow(canvas, "Fixed charge", bill.fixedCharge(), sy - 20);
                totalsRow(canvas, "Subtotal", bill.subtotal(), sy - 40);
                totalsRow(canvas, "Illustrative tax", bill.tax(), sy - 60);
                line(canvas, 320, sy - 72, RIGHT, sy - 72);
                text(canvas, "FINAL TOTAL", 10, BOLD, GREEN, 325, sy - 92);
                rightText(canvas, "INR " + money(bill.total()), 13, BOLD, GREEN, RIGHT - 8, sy - 92);

                line(canvas, LEFT, 105, RIGHT, 105);
                text(canvas, "VOLTLEDGER / JAVA OOP BILLING PLATFORM", 9, BOLD, GREEN, LEFT, 87);
                text(canvas, "Illustrative rates only. Not a utility-provider invoice or payment request.", 8, REGULAR, MUTED, LEFT, 72);
                text(canvas, "Charges are based on the original, stored invoice values.", 8, REGULAR, MUTED, LEFT, 59);
            }
            pdf.save(output);
            return output.toByteArray();
        }
    }

    private static void totalsRow(PDPageContentStream canvas, String title, BigDecimal amount, float y) throws IOException {
        text(canvas, title, 9, REGULAR, MUTED, 325, y);
        rightText(canvas, "INR " + money(amount), 10, BOLD, INK, RIGHT - 8, y);
    }
    private static String money(BigDecimal value) { return String.format(Locale.US, "%,.2f", value); }
    private static void drawRect(PDPageContentStream c, float x, float y, float w, float h, Color color) throws IOException {
        c.setNonStrokingColor(color); c.addRect(x, y, w, h); c.fill();
    }
    private static void line(PDPageContentStream c, float x1, float y1, float x2, float y2) throws IOException {
        c.setStrokingColor(new Color(222, 233, 225)); c.setLineWidth(.75f);
        c.moveTo(x1, y1); c.lineTo(x2, y2); c.stroke();
    }
    private static void text(PDPageContentStream c, String value, float size, PDType1Font font, Color color, float x, float y) throws IOException {
        c.beginText(); c.setNonStrokingColor(color); c.setFont(font, size);
        c.newLineAtOffset(x, y); c.showText(safe(value)); c.endText();
    }
    private static void rightText(PDPageContentStream c, String value, float size, PDType1Font font, Color color, float right, float y) throws IOException {
        String printable = safe(value);
        text(c, printable, size, font, color, right - font.getStringWidth(printable) * size / 1000, y);
    }
    /** Standard PDF core fonts are limited to WinAnsi; replace unsupported glyphs safely. */
    private static String safe(String source) {
        if (source == null) return "";
        String ascii = Normalizer.normalize(source, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
        return ascii.replaceAll("[^\\x20-\\x7e]", "?");
    }
    private static String fit(String source, PDType1Font font, float size, float maxWidth) throws IOException {
        String value = safe(source);
        while (!value.isEmpty() && font.getStringWidth(value) * size / 1000 > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value.equals(safe(source)) ? value : value + "...";
    }
    private static List<String> wrap(String value, PDType1Font font, float size, float width, int maxLines) throws IOException {
        String remaining = safe(value).trim().replaceAll("\\s+", " ");
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < maxLines && !remaining.isEmpty(); i++) {
            if (font.getStringWidth(remaining) * size / 1000 <= width) { lines.add(remaining); break; }
            int boundary = remaining.length();
            while (boundary > 1 && font.getStringWidth(remaining.substring(0, boundary)) * size / 1000 > width) boundary--;
            int space = remaining.lastIndexOf(' ', boundary);
            if (space > 0) boundary = space;
            lines.add(remaining.substring(0, boundary));
            remaining = remaining.substring(boundary).trim();
            if (i == maxLines - 1 && !remaining.isEmpty()) lines.set(i, fit(lines.get(i), font, size, width - 17) + "...");
        }
        return lines;
    }
}
