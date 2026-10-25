package com.voltledger.firebase;

import com.voltledger.billing.BillLine;
import com.voltledger.billing.Consumer;
import com.voltledger.billing.ElectricityBill;
import com.voltledger.data.InvoiceSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Decimal strings preserve exact values; no binary floating-point for financial fields. */
public final class FirestoreInvoiceCodec {
    private FirestoreInvoiceCodec() { }

    public static Map<String, Object> encode(ElectricityBill b) {
        var c = b.consumer();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("invoiceNumber", b.invoiceNumber());
        m.put("generatedOn", b.generatedOn().toString());
        m.put("consumerId", c.getConsumerId());
        m.put("consumerName", c.getName());
        m.put("consumerAddress", c.getAddress());
        m.put("meterNumber", c.getMeterNumber());
        m.put("tariffName", b.tariffName());
        m.put("consumedUnits", b.consumedUnits().toPlainString());
        m.put("energyCharge", b.energyCharge().toPlainString());
        m.put("fixedCharge", b.fixedCharge().toPlainString());
        m.put("subtotal", b.subtotal().toPlainString());
        m.put("tax", b.tax().toPlainString());
        m.put("total", b.total().toPlainString());
        m.put("lines", b.lines().stream().map(l -> Map.<String,Object>of(
            "label", l.label(), "units", l.units().toPlainString(),
            "rate", l.rate().toPlainString(), "amount", l.amount().toPlainString())).toList());
        return m;
    }

    public static ElectricityBill decode(Map<String, Object> m) {
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> stored = (List<Map<String,Object>>) m.get("lines");
        List<BillLine> lines = stored.stream().map(l -> new BillLine(
            (String) l.get("label"), decimal(l,"units"), decimal(l,"rate"), decimal(l,"amount"))).toList();
        return new ElectricityBill((String)m.get("invoiceNumber"),
            LocalDate.parse((String)m.get("generatedOn")),
            new Consumer((String)m.get("consumerId"), (String)m.get("consumerName"),
                (String)m.get("consumerAddress"),(String)m.get("meterNumber")),
            (String)m.get("tariffName"), decimal(m,"consumedUnits"), lines,
            decimal(m,"energyCharge"), decimal(m,"fixedCharge"),decimal(m,"subtotal"),
            decimal(m,"tax"), decimal(m,"total"));
    }

    public static InvoiceSummary summary(Map<String,Object> m) {
        return new InvoiceSummary((String)m.get("invoiceNumber"), LocalDate.parse((String)m.get("generatedOn")),
            (String)m.get("consumerId"), (String)m.get("consumerName"), (String)m.get("meterNumber"),
            (String)m.get("tariffName"), decimal(m,"consumedUnits"), decimal(m,"total"));
    }

    private static BigDecimal decimal(Map<String,Object> m, String key) {
        return new BigDecimal((String)m.get(key));
    }
}
