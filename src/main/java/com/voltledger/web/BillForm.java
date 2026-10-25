package com.voltledger.web;

import com.voltledger.billing.TariffType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/** Bean Validation checks web input; domain objects remain framework independent. */
public class BillForm {
    @NotBlank(message = "Consumer ID is required")
    @Size(max = 30, message = "Maximum 30 characters")
    @Pattern(regexp="[A-Za-z0-9][A-Za-z0-9_-]{0,29}", message="Use letters, digits, hyphens or underscores")
    private String consumerId = "VL-C1001";

    @NotBlank(message = "Consumer name is required")
    @Size(max = 100, message = "Maximum 100 characters")
    private String consumerName = "Demo Consumer";

    @NotBlank(message = "Address is required")
    @Size(max = 240, message = "Maximum 240 characters")
    private String address = "Kolkata, West Bengal";

    @NotBlank(message = "Meter number is required")
    @Size(max = 40, message = "Maximum 40 characters")
    private String meterNumber = "MTR-2026-1001";

    @NotNull(message = "Select a tariff type")
    private TariffType tariffType = TariffType.RESIDENTIAL;

    @NotNull(message = "Consumed units are required")
    @DecimalMin(value = "0.0", message = "Units cannot be negative")
    @Digits(integer = 9, fraction = 2, message = "Use up to 9 digits and 2 decimals")
    private BigDecimal consumedUnits = new BigDecimal("275");

    // Optional for public calculator; required only on admin Save Bill action.
    private String ownerUsername;
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String value) { ownerUsername = value; }

    public String getConsumerId() { return consumerId; }
    public void setConsumerId(String value) { consumerId = value; }
    public String getConsumerName() { return consumerName; }
    public void setConsumerName(String value) { consumerName = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String value) { meterNumber = value; }
    public TariffType getTariffType() { return tariffType; }
    public void setTariffType(TariffType value) { tariffType = value; }
    public BigDecimal getConsumedUnits() { return consumedUnits; }
    public void setConsumedUnits(BigDecimal value) { consumedUnits = value; }
}
