package com.voltledger.billing;

import java.math.BigDecimal;

public record BillLine(String label, BigDecimal units, BigDecimal rate, BigDecimal amount) { }
