package com.soloproject.LegalPark.dto.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AdminDailyRevenueReportResponse {  //DTO untuk Laporan Pendapatan Harian Admin

    private LocalDate date;
    private String merchantCode;
    private String merchantName;
    private BigDecimal totalRevenue;
    private long totalPaidTransactions;
    private long totalFailedTransactions;

    
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalPaidTransactions() {
        return totalPaidTransactions;
    }

    public void setTotalPaidTransactions(long totalPaidTransactions) {
        this.totalPaidTransactions = totalPaidTransactions;
    }

    public long getTotalFailedTransactions() {
        return totalFailedTransactions;
    }

    public void setTotalFailedTransactions(long totalFailedTransactions) {
        this.totalFailedTransactions = totalFailedTransactions;
    }


}
