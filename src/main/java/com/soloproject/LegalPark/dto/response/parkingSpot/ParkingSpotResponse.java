package com.soloproject.LegalPark.dto.response.parkingSpot;

import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;

public class ParkingSpotResponse {
    private String id;
    private String spotNumber;
    private String spotType;
    private String status;
    private Integer floor;


    private MerchantResponse merchant;

    public MerchantResponse getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantResponse merchant) {
        this.merchant = merchant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getSpotType() {
        return spotType;
    }

    public void setSpotType(String spotType) {
        this.spotType = spotType;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
