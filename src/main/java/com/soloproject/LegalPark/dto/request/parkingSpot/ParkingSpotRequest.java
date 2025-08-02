package com.soloproject.LegalPark.dto.request.parkingSpot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParkingSpotRequest {
    @NotBlank(message = "Spot number is required")
    @Size(max = 20, message = "Spot number cannot exceed 20 characters")
    private String spotNumber; // Misalnya "A01", "B-P05"

    private Integer floor;

    @NotBlank(message = "Spot type is required (e.g., CAR, MOTORCYCLE, UNIVERSAL)")
    private String spotType;

    @NotBlank(message = "Merchant code is required to associate the parking spot")
    private String merchantCode;

    public @NotBlank(message = "Spot number is required") @Size(max = 20, message = "Spot number cannot exceed 20 characters") String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(@NotBlank(message = "Spot number is required") @Size(max = 20, message = "Spot number cannot exceed 20 characters") String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public @NotBlank(message = "Spot type is required (e.g., CAR, MOTORCYCLE, UNIVERSAL)") String getSpotType() {
        return spotType;
    }

    public void setSpotType(@NotBlank(message = "Spot type is required (e.g., CAR, MOTORCYCLE, UNIVERSAL)") String spotType) {
        this.spotType = spotType;
    }

    public @NotBlank(message = "Merchant code is required to associate the parking spot") String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(@NotBlank(message = "Merchant code is required to associate the parking spot") String merchantCode) {
        this.merchantCode = merchantCode;
    }
}
