package com.voltledger.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/** Admin-managed meter/consumer identity; distinct from app login accounts. */
public class ConsumerRecordForm {
    @NotBlank @Size(max=30) @Pattern(regexp="[A-Za-z0-9][A-Za-z0-9_-]{0,29}", message="Use letters, digits, hyphens or underscores")
    private String consumerId="";
    @NotBlank @Size(max=100)
    private String fullName="";
    @NotBlank @Size(max=240)
    private String address="";
    @NotBlank @Size(max=40)
    private String meterNumber="";
    @NotBlank(message="Select the consumer login to own this meter")
    private String ownerUsername="";

    public String getConsumerId(){return consumerId;} public void setConsumerId(String value){consumerId=value;}
    public String getFullName(){return fullName;} public void setFullName(String value){fullName=value;}
    public String getAddress(){return address;} public void setAddress(String value){address=value;}
    public String getMeterNumber(){return meterNumber;} public void setMeterNumber(String value){meterNumber=value;}
    public String getOwnerUsername(){return ownerUsername;} public void setOwnerUsername(String value){ownerUsername=value;}
}
