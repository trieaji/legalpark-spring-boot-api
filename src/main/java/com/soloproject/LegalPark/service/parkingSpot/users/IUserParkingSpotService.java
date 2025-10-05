package com.soloproject.LegalPark.service.parkingSpot.users;

import com.soloproject.LegalPark.dto.request.parkingSpot.AvailableSpotFilterRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IUserParkingSpotService {
    // 1. View Available Parking Spaces (GET Available)
    ResponseEntity<Object> userGetAvailableParkingSpots(AvailableSpotFilterRequest filter);


    // 2. View Parking Spaces by Merchant (GET by Merchant for User)
    ResponseEntity<Object> userGetParkingSpotsByMerchant(String merchantCode);


}
