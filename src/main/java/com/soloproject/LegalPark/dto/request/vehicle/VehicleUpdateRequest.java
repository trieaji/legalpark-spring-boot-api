package com.soloproject.LegalPark.dto.request.vehicle;

import jakarta.validation.constraints.Size;

public class VehicleUpdateRequest {
    @Size(max = 20, message = "License plate cannot exceed 20 characters")
    private String licensePlate;

    @Size(max = 50, message = "Vehicle type cannot exceed 50 characters")
    private String type;

    private String merchantCode;
    private String ownerId;


    public @Size(max = 20, message = "License plate cannot exceed 20 characters") String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(@Size(max = 20, message = "License plate cannot exceed 20 characters") String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public @Size(max = 50, message = "Vehicle type cannot exceed 50 characters") String getType() {
        return type;
    }

    public void setType(@Size(max = 50, message = "Vehicle type cannot exceed 50 characters") String type) {
        this.type = type;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

}
