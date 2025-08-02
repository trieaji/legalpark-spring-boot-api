package com.soloproject.LegalPark.dto.response.report;

import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserParkingHistoryReportResponse { //DTO untuk Laporan Riwayat Parkir Pengguna

    private String transactionId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal totalCost;
    private String status;
    private String paymentStatus;
    private VehicleResponse vehicle;
    private ParkingSpotResponse parkingSpot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public VehicleResponse getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleResponse vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSpotResponse getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpotResponse parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


}
