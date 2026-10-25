package com.voltledger.billing;

import java.math.BigDecimal;
import java.util.List;

/** Abstract class demonstrating inheritance and abstraction. */
public abstract class TariffPlan {
    public abstract String getDisplayName();
    public abstract BigDecimal getFixedCharge();
    public abstract List<RateSlab> getSlabs();
}
