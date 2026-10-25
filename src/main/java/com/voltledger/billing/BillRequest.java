package com.voltledger.billing;

import java.math.BigDecimal;

public record BillRequest(Consumer consumer, TariffType tariffType, BigDecimal consumedUnits) { }
