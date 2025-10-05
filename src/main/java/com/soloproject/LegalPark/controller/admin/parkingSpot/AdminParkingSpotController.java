package com.soloproject.LegalPark.controller.admin.parkingSpot;

import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotRequest;
import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotUpdateRequest;
import com.soloproject.LegalPark.service.parkingSpot.admin.IAdminParkingSpotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Parking Spot API", description = "Endpoint untuk Admin mengelola informasi slot parkir")
public class AdminParkingSpotController {
    @Autowired
    IAdminParkingSpotService iAdminParkingSpotService;




    @PostMapping("/parking-spots")
    public ResponseEntity<Object> createParkingSpot(@Valid @RequestBody ParkingSpotRequest request) {
        return iAdminParkingSpotService.adminCreateParkingSpot(request);
    }




    @GetMapping("/parking-spots")
    public ResponseEntity<Object> getAllParkingSpots() {
        return iAdminParkingSpotService.adminGetAllParkingSpots();
    }




    @GetMapping("/parking-spots/{id}")
    public ResponseEntity<Object> getParkingSpotById(@PathVariable("id") String id) {
        return iAdminParkingSpotService.adminGetParkingSpotById(id);
    }




    @PatchMapping("/parking-spots/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable("id") String id,
                                                    @Valid @RequestBody ParkingSpotUpdateRequest request) {
        return iAdminParkingSpotService.adminUpdateParkingSpot(id, request);
    }




    @DeleteMapping("/parking-spots/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable("id") String id) {
        return iAdminParkingSpotService.adminDeleteParkingSpot(id);
    }




    @GetMapping("/parking-spots/by-merchant/{merchantIdentifier}")
    public ResponseEntity<Object> getParkingSpotsByMerchant(@PathVariable("merchantIdentifier") String merchantIdentifier) {
        return iAdminParkingSpotService.adminGetParkingSpotsByMerchant(merchantIdentifier);
    }
}
