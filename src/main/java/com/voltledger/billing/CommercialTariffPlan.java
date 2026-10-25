package com.voltledger.billing;

import java.math.BigDecimal;
import java.util.List;

public final class CommercialTariffPlan extends TariffPlan {
    private static final List<RateSlab> SLABS = List.of(
        new RateSlab(new BigDecimal("100"), new BigDecimal("7.00")),
        new RateSlab(new BigDecimal("200"), new BigDecimal("9.00")),
        new RateSlab(null, new BigDecimal("11.00"))
    );

    @Override public String getDisplayName() { return "Commercial"; }
    @Override public BigDecimal getFixedCharge() { return new BigDecimal("150.00"); }
    @Override public List<RateSlab> getSlabs() { return SLABS; }
}
