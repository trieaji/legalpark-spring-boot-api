package com.soloproject.LegalPark.dto.request.parkingSpot;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class AvailableSpotFilterRequest {

    @Size(min = 1, max = 50, message = "Merchant Code must be between 1 and 50 characters")
    private String merchantCode;

    @Size(min = 3, max = 20, message = "Spot type must be between 3 and 20 characters (e.g., CAR, MOTORCYCLE)")
    private String spotType;

    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floor;

    public @Size(min = 1, max = 50, message = "Merchant Code must be between 1 and 50 characters") String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(@Size(min = 1, max = 50, message = "Merchant Code must be between 1 and 50 characters") String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public @Size(min = 3, max = 20, message = "Spot type must be between 3 and 20 characters (e.g., CAR, MOTORCYCLE)") String getSpotType() {
        return spotType;
    }

    public void setSpotType(@Size(min = 3, max = 20, message = "Spot type must be between 3 and 20 characters (e.g., CAR, MOTORCYCLE)") String spotType) {
        this.spotType = spotType;
    }

    public @Min(value = 0, message = "Floor number cannot be negative") Integer getFloor() {
        return floor;
    }

    public void setFloor(@Min(value = 0, message = "Floor number cannot be negative") Integer floor) {
        this.floor = floor;
    }

}
