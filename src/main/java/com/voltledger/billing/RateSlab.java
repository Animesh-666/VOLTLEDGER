package com.voltledger.billing;

import java.math.BigDecimal;

/** upperLimit is the cumulative upper threshold, null for unlimited. */
public record RateSlab(BigDecimal upperLimit, BigDecimal ratePerUnit) {
    public RateSlab {
        if (ratePerUnit == null || ratePerUnit.signum() < 0) {
            throw new IllegalArgumentException("Rate must be zero or greater");
        }
    }
}
