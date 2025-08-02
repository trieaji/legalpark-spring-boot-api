package com.soloproject.LegalPark.dto.request.verificationCode;

import jakarta.validation.constraints.NotBlank;

public class PaymentVerificationCodeRequest {
    @NotBlank(message = "User ID cannot be blank")
    private String userId;


    @NotBlank(message = "Parking transaction ID cannot be empty")
    private String parkingTransactionId;

    public @NotBlank(message = "Parking transaction ID cannot be empty") String getParkingTransactionId() {
        return parkingTransactionId;
    }

    public void setParkingTransactionId(@NotBlank(message = "Parking transaction ID cannot be empty") String parkingTransactionId) {
        this.parkingTransactionId = parkingTransactionId;
    }

    public @NotBlank(message = "User ID cannot be blank") String getUserId() {
        return userId;
    }

    public void setUserId(@NotBlank(message = "User ID cannot be blank") String userId) {
        this.userId = userId;
    }
}
