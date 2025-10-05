package com.soloproject.LegalPark.controller.users.parkingSpot;

import com.soloproject.LegalPark.dto.request.parkingSpot.AvailableSpotFilterRequest;
import com.soloproject.LegalPark.service.parkingSpot.users.IUserParkingSpotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Parking Spot API", description = "Endpoint untuk pengguna (User) melihat informasi slot parkir")
public class UserParkingSpotController {

    @Autowired
    private IUserParkingSpotService iUserParkingSpotService;



    //  Endpoint for users to search for available parking spots with various filters.
    //  Filters can be merchantCode, spotType, and floor.
    //  Example: GET /api/v1/users/parking-spots/available?merchantCode=MERCH001&spotType=CAR&floor=2
    @GetMapping("/parking-spots/available")
    public ResponseEntity<Object> getAvailableParkingSpots(
            @Valid @ModelAttribute AvailableSpotFilterRequest filter) {
        // @ModelAttribute digunakan untuk binding query parameters ke DTO
        // @Valid akan memicu validasi sesuai anotasi di dalam AvailableSpotFilterRequest
        return iUserParkingSpotService.userGetAvailableParkingSpots(filter);
    }



    // Endpoint for users to view all parking spots (available and occupied) at a merchant based on merchant code.
    // Example: GET /api/v1/users/parking-spots/by-merchant/MERCH001
    @GetMapping("/parking-spots/by-merchant/{merchantCode}")
    public ResponseEntity<Object> getParkingSpotsByMerchant(@PathVariable("merchantCode") String merchantCode) {
        return iUserParkingSpotService.userGetParkingSpotsByMerchant(merchantCode);
    }
}
