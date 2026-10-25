package com.voltledger.billing;

import java.math.BigDecimal;

/** Strategy interface to support different future charge or tax policies. */
public interface ChargeRule {
    BigDecimal apply(BigDecimal baseAmount);
}
