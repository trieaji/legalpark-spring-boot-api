package com.soloproject.LegalPark.helper;

import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.entity.ParkingSpot;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParkingSpotResponseMapper {

    @Autowired
    private ModelMapper modelMapper;

    public ParkingSpotResponse mapToParkingSpotResponse(ParkingSpot parkingSpot) {
        ParkingSpotResponse response = new ParkingSpotResponse();
        response.setId(parkingSpot.getId());
        response.setSpotNumber(parkingSpot.getSpotNumber());
        response.setSpotType(parkingSpot.getSpotType().name());
        response.setStatus(parkingSpot.getStatus().name());
        response.setFloor(parkingSpot.getFloor());

        if (parkingSpot.getMerchant() != null) {
            response.setMerchant(modelMapper.map(parkingSpot.getMerchant(), MerchantResponse.class));
        }
        return response;
    }
}
