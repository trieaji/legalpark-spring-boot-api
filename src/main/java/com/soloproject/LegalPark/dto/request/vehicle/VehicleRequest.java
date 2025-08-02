package com.soloproject.LegalPark.dto.request.vehicle;

public class VehicleRequest {
    private String licensePlate;
    private String type;
    private String ownerId;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public String getMerchantCode() {
//        return merchantCode;
//    }
//
//    public void setMerchantCode(String merchantCode) {
//        this.merchantCode = merchantCode;
//    }
}
