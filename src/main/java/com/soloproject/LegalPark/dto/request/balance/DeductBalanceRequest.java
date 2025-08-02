package com.soloproject.LegalPark.dto.request.balance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DeductBalanceRequest {
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;


    public DeductBalanceRequest() {}

    public DeductBalanceRequest(String userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public @NotBlank(message = "User ID cannot be blank") String getUserId() {
        return userId;
    }

    public void setUserId(@NotBlank(message = "User ID cannot be blank") String userId) {
        this.userId = userId;
    }

    public @NotNull(message = "Amount cannot be null") @Positive(message = "Amount must be positive") BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "Amount cannot be null") @Positive(message = "Amount must be positive") BigDecimal amount) {
        this.amount = amount;
    }
}
