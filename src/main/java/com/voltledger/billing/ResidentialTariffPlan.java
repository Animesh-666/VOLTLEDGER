package com.voltledger.billing;

import java.math.BigDecimal;
import java.util.List;

public final class ResidentialTariffPlan extends TariffPlan {
    private static final List<RateSlab> SLABS = List.of(
        new RateSlab(new BigDecimal("100"), new BigDecimal("4.00")),
        new RateSlab(new BigDecimal("200"), new BigDecimal("6.00")),
        new RateSlab(null, new BigDecimal("8.00"))
    );

    @Override public String getDisplayName() { return "Residential"; }
    @Override public BigDecimal getFixedCharge() { return new BigDecimal("75.00"); }
    @Override public List<RateSlab> getSlabs() { return SLABS; }
}
