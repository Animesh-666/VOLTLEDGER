package com.voltledger.analytics;

import com.voltledger.data.InvoiceSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BillingAnalyticsServiceTest {
    private static InvoiceSummary sample(String id, String date, String units, String amount, String tariff) {
        return new InvoiceSummary(id, LocalDate.parse(date), "C-" + id, "Demo", "M-" + id,
            tariff, new BigDecimal(units), new BigDecimal(amount));
    }

    @Test void groupsSavedBillsByMonthAndKeepsAllTimeTotals() {
        var bills = List.of(
            sample("VL-1", "2026-08-12", "100.00", "498.75", "Residential"),
            sample("VL-2", "2026-10-02", "275.00", "1758.75", "Residential"),
            sample("VL-3", "2026-10-11", "150.00", "1239.00", "Commercial"));
        var overview = new BillingAnalyticsService().summarize(bills, YearMonth.of(2026, 10));
        assertEquals(3, overview.invoices());
        assertEquals(0, new BigDecimal("525").compareTo(overview.totalUnits()));
        assertEquals(0, new BigDecimal("3496.50").compareTo(overview.totalAmount()));
        assertEquals(2, overview.residentialCount());
        assertEquals(1, overview.commercialCount());
        assertEquals(6, overview.months().size());
        assertEquals(0, new BigDecimal("425").compareTo(overview.months().get(5).units()));
        assertEquals(100, overview.months().get(5).heightPercent());
        assertEquals(0, new BigDecimal("100").compareTo(overview.months().get(3).units()));
    }

    @Test void emptyHistoryIsStillRenderableWithoutDivisionByZero() {
        var summary = new BillingAnalyticsService().summarize(List.of(), YearMonth.of(2026, 10));
        assertEquals(0, summary.invoices());
        assertEquals(0, summary.averageInvoice().signum());
        assertTrue(summary.months().stream().allMatch(m -> m.heightPercent() == 0));
    }
}
