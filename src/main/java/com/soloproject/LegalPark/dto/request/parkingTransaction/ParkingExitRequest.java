package com.soloproject.LegalPark.dto.request.parkingTransaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParkingExitRequest {
    @NotBlank(message = "License plate cannot be empty")
    @Size(min = 1, max = 20, message = "License plate must be between 1 and 20 characters")
    private String licensePlate;


    @NotBlank(message = "Merchant code cannot be empty")
    @Size(min = 1, max = 50, message = "Merchant code must be between 1 and 50 characters")
    private String merchantCode;


    @NotBlank(message = "Verification code cannot be empty")
    @Size(min = 4, max = 6, message = "Verification code must be minimum 4 characters")
    private String verificationCode;



    public @NotBlank(message = "License plate cannot be empty") @Size(min = 1, max = 20, message = "License plate must be between 1 and 20 characters") String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(@NotBlank(message = "License plate cannot be empty") @Size(min = 1, max = 20, message = "License plate must be between 1 and 20 characters") String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public @NotBlank(message = "Merchant code cannot be empty") @Size(min = 1, max = 50, message = "Merchant code must be between 1 and 50 characters") String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(@NotBlank(message = "Merchant code cannot be empty") @Size(min = 1, max = 50, message = "Merchant code must be between 1 and 50 characters") String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public @NotBlank(message = "Verification code cannot be empty") @Size(min = 4, max = 6, message = "Verification code must be minimum 4 characters") String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(@NotBlank(message = "Verification code cannot be empty") @Size(min = 4, max = 6, message = "Verification code must be minimum 4 characters") String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
