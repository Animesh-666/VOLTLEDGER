package com.voltledger.analytics;

import com.voltledger.data.InvoiceSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Calculates report statistics in Java, independent of HTTP and SQL. */
@Service
public class BillingAnalyticsService {
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);

    public DashboardOverview summarize(List<InvoiceSummary> source, YearMonth currentMonth) {
        List<InvoiceSummary> invoices = List.copyOf(source);
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal units = BigDecimal.ZERO;
        int residential = 0;
        int commercial = 0;
        for (InvoiceSummary invoice : invoices) {
            amount = amount.add(invoice.total());
            units = units.add(invoice.units());
            if ("Residential".equalsIgnoreCase(invoice.tariffName())) residential++;
            if ("Commercial".equalsIgnoreCase(invoice.tariffName())) commercial++;
        }
        List<MonthlyUsage> months = new ArrayList<>();
        BigDecimal peak = BigDecimal.ZERO;
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            BigDecimal monthlyUnits = BigDecimal.ZERO;
            BigDecimal monthlyAmount = BigDecimal.ZERO;
            int count = 0;
            for (InvoiceSummary invoice : invoices) {
                if (YearMonth.from(invoice.generatedOn()).equals(month)) {
                    monthlyUnits = monthlyUnits.add(invoice.units());
                    monthlyAmount = monthlyAmount.add(invoice.total());
                    count++;
                }
            }
            if (monthlyUnits.compareTo(peak) > 0) peak = monthlyUnits;
            months.add(new MonthlyUsage(month.format(MONTH_LABEL), monthlyUnits, monthlyAmount, count, 0));
        }
        List<MonthlyUsage> chart = new ArrayList<>();
        for (MonthlyUsage month : months) {
            int height = peak.signum() == 0 ? 0 : month.units()
                .multiply(BigDecimal.valueOf(100))
                .divide(peak, 0, RoundingMode.HALF_UP)
                .intValue();
            chart.add(new MonthlyUsage(month.label(), month.units(), month.amount(), month.invoiceCount(), height));
        }
        BigDecimal average = invoices.isEmpty() ? BigDecimal.ZERO.setScale(2) :
            amount.divide(BigDecimal.valueOf(invoices.size()), 2, RoundingMode.HALF_UP);
        return new DashboardOverview(invoices.size(), amount, units, average, residential, commercial,
            List.copyOf(chart));
    }

    public record MonthlyUsage(String label, BigDecimal units, BigDecimal amount,
                               int invoiceCount, int heightPercent) { }
    public record DashboardOverview(int invoices, BigDecimal totalAmount, BigDecimal totalUnits,
                                    BigDecimal averageInvoice, int residentialCount,
                                    int commercialCount, List<MonthlyUsage> months) { }
}
