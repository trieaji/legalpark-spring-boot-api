package com.soloproject.LegalPark.helper;

import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.dto.response.parkingTransaction.ParkingTransactionResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.ParkingTransaction;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParkingTransactionResponseMapper {

    @Autowired
    private ModelMapper modelMapper;

    public ParkingTransactionResponse mapToParkingTransactionResponse(ParkingTransaction transaction) {
        ParkingTransactionResponse response = new ParkingTransactionResponse();
        response.setId(transaction.getId());
        response.setEntryTime(transaction.getEntryTime());
        response.setExitTime(transaction.getExitTime());
        response.setTotalCost(transaction.getTotalCost());
        response.setStatus(transaction.getStatus().name());
        response.setPaymentStatus(transaction.getPaymentStatus().name());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());

        // Mapping Vehicle DTO
        if (transaction.getVehicle() != null) {
            response.setVehicle(modelMapper.map(transaction.getVehicle(), VehicleResponse.class));
        }

        // Mapping ParkingSpot DTO
        if (transaction.getParkingSpot() != null) {
            ParkingSpotResponse spotResponse = new ParkingSpotResponse();
            spotResponse.setId(transaction.getParkingSpot().getId());
            spotResponse.setSpotNumber(transaction.getParkingSpot().getSpotNumber());
            spotResponse.setSpotType(transaction.getParkingSpot().getSpotType().name());
            spotResponse.setStatus(transaction.getParkingSpot().getStatus().name());
            spotResponse.setFloor(transaction.getParkingSpot().getFloor());

            // Mapping Merchant DTO dalam ParkingSpotResponse
            if (transaction.getParkingSpot().getMerchant() != null) {
                spotResponse.setMerchant(modelMapper.map(transaction.getParkingSpot().getMerchant(), MerchantResponse.class));
            }
            response.setParkingSpot(spotResponse);
        }
        return response;
    }
}
