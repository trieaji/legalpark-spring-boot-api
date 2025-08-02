package com.soloproject.LegalPark.dto.request.parkingSpot;

import jakarta.validation.constraints.Size;

public class ParkingSpotUpdateRequest {
    @Size(max = 20, message = "Spot number cannot exceed 20 characters")
    private String spotNumber;
    private Integer floor;

    private String spotType;
    private String status;


    private String merchantCode;
    
    public @Size(max = 20, message = "Spot number cannot exceed 20 characters") String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(@Size(max = 20, message = "Spot number cannot exceed 20 characters") String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getSpotType() {
        return spotType;
    }

    public void setSpotType(String spotType) {
        this.spotType = spotType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

}
