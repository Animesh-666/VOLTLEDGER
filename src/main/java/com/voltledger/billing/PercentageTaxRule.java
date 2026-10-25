package com.voltledger.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PercentageTaxRule implements ChargeRule {
    private final BigDecimal rate;

    public PercentageTaxRule(BigDecimal rate) {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tax fraction must be between 0 and 1");
        }
        this.rate = rate;
    }

    @Override
    public BigDecimal apply(BigDecimal baseAmount) {
        return baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
