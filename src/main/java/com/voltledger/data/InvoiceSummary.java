package com.voltledger.data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Read-only invoice row for the dashboards. */
public record InvoiceSummary(String invoiceNumber, LocalDate generatedOn,
    String consumerId, String consumerName, String meterNumber,
    String tariffName, BigDecimal units, BigDecimal total) { }
