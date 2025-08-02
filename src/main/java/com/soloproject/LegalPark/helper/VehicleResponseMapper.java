package com.soloproject.LegalPark.helper;

import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.dto.response.users.UserBasicResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.Vehicle;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleResponseMapper  {

    @Autowired
    private ModelMapper modelMapper;

    public VehicleResponse mapToVehicleResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setType(vehicle.getType().name()); // Konversi Enum ke String

//        ModelMapper modelMapper = new ModelMapper(); //Aku comment karena sudah ada private ModelMapper modelMapper;
        if (vehicle.getOwner() != null) {
            response.setOwner(modelMapper.map(vehicle.getOwner(), UserBasicResponse.class));
        }
        return response;
    }
}
