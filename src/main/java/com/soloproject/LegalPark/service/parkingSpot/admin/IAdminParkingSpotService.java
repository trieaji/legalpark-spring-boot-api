package com.soloproject.LegalPark.service.parkingSpot.admin;

import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotRequest;
import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotUpdateRequest;
import org.springframework.http.ResponseEntity;

public interface IAdminParkingSpotService {
    // 1. Registering a New Parking Slot (CREATE)
    ResponseEntity<Object> adminCreateParkingSpot(ParkingSpotRequest request);

    // 2. View All Parking Spaces (GET ALL)
    ResponseEntity<Object> adminGetAllParkingSpots();

    // 3. View Details of Specific Parking Spaces by ID (GET by ID)
    ResponseEntity<Object> adminGetParkingSpotById(String id);

    // 4. Updating Parking Slot Data (UPDATE)
    ResponseEntity<Object> adminUpdateParkingSpot(String id, ParkingSpotUpdateRequest request);

    // 5. Deleting a Parking Slot (DELETE)
    ResponseEntity<Object> adminDeleteParkingSpot(String id);

    // 6. View Parking Spaces by Merchant (Optional, but very useful)
    // The parameter can be merchantId or merchantCode.
    ResponseEntity<Object> adminGetParkingSpotsByMerchant(String merchantIdentifier); // merchantId/merchantCode


}
