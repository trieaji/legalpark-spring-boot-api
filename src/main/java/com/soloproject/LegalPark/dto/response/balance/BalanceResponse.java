package com.soloproject.LegalPark.dto.response.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BalanceResponse {
    private String userId;

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    private BigDecimal currentBalance;
    private String status;
    private String message;
    private LocalDateTime timestamp;


    public BalanceResponse() {}

    public BalanceResponse(String userId, BigDecimal currentBalance, String status, String message, LocalDateTime timestamp) {
        this.userId = userId;
        this.currentBalance = currentBalance;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}
