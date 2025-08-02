package com.soloproject.LegalPark.dto.response.report;

import java.math.BigDecimal;

public class UserSummaryReportResponse {  // DTO untuk Laporan Ringkasan Pengguna (Opsional, tapi Berguna)

    private String userId;
    private String userName;
    private long totalParkingSessions;
    private BigDecimal totalCostSpent;
    private BigDecimal currentBalance;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTotalParkingSessions() {
        return totalParkingSessions;
    }

    public void setTotalParkingSessions(long totalParkingSessions) {
        this.totalParkingSessions = totalParkingSessions;
    }

    public BigDecimal getTotalCostSpent() {
        return totalCostSpent;
    }

    public void setTotalCostSpent(BigDecimal totalCostSpent) {
        this.totalCostSpent = totalCostSpent;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

}
