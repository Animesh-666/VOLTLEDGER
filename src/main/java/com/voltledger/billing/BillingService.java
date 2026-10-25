package com.voltledger.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** All tariff and monetary calculations live in Java, never in browser JavaScript. */
@Service
public final class BillingService {
    private static final ChargeRule DEMO_TAX = new PercentageTaxRule(new BigDecimal("0.05"));

    public ElectricityBill calculate(BillRequest request) {
        if (request == null || request.consumer() == null || request.tariffType() == null
                || request.consumedUnits() == null) {
            throw new IllegalArgumentException("Complete consumer and billing details are required");
        }
        BigDecimal units = request.consumedUnits();
        if (units.signum() < 0 || units.scale() > 2) {
            throw new IllegalArgumentException("Consumed units must be non-negative with up to 2 decimals");
        }

        // Polymorphism: both plan types are handled through their common base class.
        TariffPlan plan = switch (request.tariffType()) {
            case RESIDENTIAL -> new ResidentialTariffPlan();
            case COMMERCIAL -> new CommercialTariffPlan();
        };

        BigDecimal billedUnits = BigDecimal.ZERO;
        BigDecimal energyCharge = BigDecimal.ZERO;
        List<BillLine> lines = new ArrayList<>();
        int index = 1;
        for (RateSlab slab : plan.getSlabs()) {
            BigDecimal remaining = units.subtract(billedUnits);
            if (remaining.signum() <= 0) break;
            BigDecimal availableInSlab = slab.upperLimit() == null
                ? remaining : slab.upperLimit().subtract(billedUnits);
            BigDecimal slabUnits = remaining.min(availableInSlab);
            if (slabUnits.signum() <= 0) continue;
            BigDecimal amount = money(slabUnits.multiply(slab.ratePerUnit()));
            lines.add(new BillLine("Slab " + index, slabUnits, slab.ratePerUnit(), amount));
            billedUnits = billedUnits.add(slabUnits);
            energyCharge = energyCharge.add(amount);
            index++;
        }
        BigDecimal fixed = money(plan.getFixedCharge());
        BigDecimal subtotal = money(energyCharge.add(fixed));
        BigDecimal tax = DEMO_TAX.apply(subtotal);
        return new ElectricityBill(
            "VL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            LocalDate.now(), request.consumer(), plan.getDisplayName(), units,
            List.copyOf(lines), money(energyCharge), fixed, subtotal,
            tax, money(subtotal.add(tax))
        );
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
