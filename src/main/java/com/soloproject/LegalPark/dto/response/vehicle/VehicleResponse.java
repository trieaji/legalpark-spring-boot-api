package com.soloproject.LegalPark.dto.response.vehicle;

import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.dto.response.users.UserBasicResponse;

public class VehicleResponse {
    private String id;
    private String licensePlate;
    private String type;
    private UserBasicResponse owner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserBasicResponse getOwner() {
        return owner;
    }

    public void setOwner(UserBasicResponse owner) {
        this.owner = owner;
    }
    
    
}
