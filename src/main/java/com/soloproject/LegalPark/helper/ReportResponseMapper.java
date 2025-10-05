package com.soloproject.LegalPark.helper;

import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.dto.response.report.UserParkingHistoryReportResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.ParkingTransaction;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportResponseMapper {

    @Autowired
    private ModelMapper modelMapper;

    public UserParkingHistoryReportResponse mapToUserParkingHistoryReportResponse(ParkingTransaction transaction) {
        UserParkingHistoryReportResponse response = modelMapper.map(transaction, UserParkingHistoryReportResponse.class);
        response.setTransactionId(transaction.getId());
        response.setStatus(transaction.getStatus().name());
        response.setPaymentStatus(transaction.getPaymentStatus().name());


        if (transaction.getVehicle() != null) {
            response.setVehicle(modelMapper.map(transaction.getVehicle(), VehicleResponse.class));

        }


        if (transaction.getParkingSpot() != null) {
            response.setParkingSpot(modelMapper.map(transaction.getParkingSpot(), ParkingSpotResponse.class));
        }

        return response;
    }
}
