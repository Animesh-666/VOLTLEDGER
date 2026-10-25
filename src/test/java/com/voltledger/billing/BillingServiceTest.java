package com.voltledger.billing;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {
    private final BillingService service = new BillingService();
    private final Consumer consumer = new Consumer("C101", "Demo", "Kolkata", "M100");

    private ElectricityBill calculate(TariffType tariff, String units) {
        return service.calculate(new BillRequest(consumer, tariff, new BigDecimal(units)));
    }

    @Test void residential275UnitsProducesExpectedTotal() {
        ElectricityBill bill = calculate(TariffType.RESIDENTIAL, "275");
        assertEquals(0, new BigDecimal("1600.00").compareTo(bill.energyCharge()));
        assertEquals(0, new BigDecimal("75.00").compareTo(bill.fixedCharge()));
        assertEquals(0, new BigDecimal("83.75").compareTo(bill.tax()));
        assertEquals(0, new BigDecimal("1758.75").compareTo(bill.total()));
        assertEquals(3, bill.lines().size());
    }

    @Test void zeroUnitsOnlyPayFixedChargeAndIllustrativeTax() {
        ElectricityBill bill = calculate(TariffType.RESIDENTIAL, "0");
        assertEquals(0, new BigDecimal("0.00").compareTo(bill.energyCharge()));
        assertEquals(0, new BigDecimal("78.75").compareTo(bill.total()));
    }

    @Test void exactly100UnitsUsesFirstSlabOnly() {
        ElectricityBill bill = calculate(TariffType.RESIDENTIAL, "100");
        assertEquals(1, bill.lines().size());
        assertEquals(0, new BigDecimal("400.00").compareTo(bill.energyCharge()));
    }

    @Test void commercialTariffUsesDifferentRates() {
        ElectricityBill bill = calculate(TariffType.COMMERCIAL, "100");
        assertEquals(0, new BigDecimal("700.00").compareTo(bill.energyCharge()));
        assertEquals(0, new BigDecimal("892.50").compareTo(bill.total()));
    }

    @Test void negativeUnitsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> calculate(TariffType.RESIDENTIAL, "-1"));
    }
}
