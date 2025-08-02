package com.soloproject.LegalPark.dto.response.report;

public class AdminParkingSpotOccupancyReportResponse { // DTO untuk Laporan Tingkat Hunian Slot Parkir Admin

    private String spotId;
    private String spotNumber;
    private String spotType;
    private String currentStatus;
    private String merchantCode;
    private String merchantName;
    private String currentVehicleLicensePlate;
    private String currentVehicleType;
    private String currentOccupantUserName;

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public String getSpotType() {
        return spotType;
    }

    public void setSpotType(String spotType) {
        this.spotType = spotType;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
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

    public String getCurrentVehicleLicensePlate() {
        return currentVehicleLicensePlate;
    }

    public void setCurrentVehicleLicensePlate(String currentVehicleLicensePlate) {
        this.currentVehicleLicensePlate = currentVehicleLicensePlate;
    }

    public String getCurrentVehicleType() {
        return currentVehicleType;
    }

    public void setCurrentVehicleType(String currentVehicleType) {
        this.currentVehicleType = currentVehicleType;
    }

    public String getCurrentOccupantUserName() {
        return currentOccupantUserName;
    }

    public void setCurrentOccupantUserName(String currentOccupantUserName) {
        this.currentOccupantUserName = currentOccupantUserName;
    }
}
