package com.soloproject.LegalPark.service.parkingSpot.users;

import com.soloproject.LegalPark.dto.request.parkingSpot.AvailableSpotFilterRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IUserParkingSpotService {
    // 1. Melihat Slot Parkir yang Tersedia (GET Available)
    ResponseEntity<Object> userGetAvailableParkingSpots(AvailableSpotFilterRequest filter);


    // 2. Melihat Slot Parkir berdasarkan Merchant (GET by Merchant for User)
    ResponseEntity<Object> userGetParkingSpotsByMerchant(String merchantCode);

//    Untuk users Mungkin tidak perlu mengelola slot, tetapi perlu melihat ketersediaan slot atau mencari slot yang tersedia.
}
