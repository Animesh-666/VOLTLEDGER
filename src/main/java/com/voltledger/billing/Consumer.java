package com.voltledger.billing;

/** A consumer is a domain object; no database dependency is needed. */
public final class Consumer {
    private final String consumerId;
    private final String name;
    private final String address;
    private final String meterNumber;

    public Consumer(String consumerId, String name, String address, String meterNumber) {
        this.consumerId = requireText(consumerId, "Consumer ID");
        this.name = requireText(name, "Consumer name");
        this.address = requireText(address, "Address");
        this.meterNumber = requireText(meterNumber, "Meter number");
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value.trim();
    }

    public String getConsumerId() { return consumerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getMeterNumber() { return meterNumber; }
}
