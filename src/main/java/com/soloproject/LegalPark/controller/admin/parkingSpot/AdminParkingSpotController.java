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
@RequestMapping("/api/v1/admin") // Base path untuk semua endpoint admin
@Tag(name = "Admin Parking Spot API", description = "Endpoint untuk Admin mengelola informasi slot parkir")
public class AdminParkingSpotController {
    @Autowired
    IAdminParkingSpotService iAdminParkingSpotService;



//     Endpoint untuk admin mendaftarkan slot parkir baru.
//     Contoh: POST /api/v1/admin/parking-spots
    @PostMapping("/parking-spots")
    public ResponseEntity<Object> createParkingSpot(@Valid @RequestBody ParkingSpotRequest request) {
        return iAdminParkingSpotService.adminCreateParkingSpot(request);
    }



//     Endpoint untuk admin melihat semua slot parkir yang terdaftar.
//     Contoh: GET /api/v1/admin/parking-spots
    @GetMapping("/parking-spots")
    public ResponseEntity<Object> getAllParkingSpots() {
        return iAdminParkingSpotService.adminGetAllParkingSpots();
    }



//     Endpoint untuk admin melihat detail slot parkir berdasarkan ID.
//     Contoh: GET /api/v1/admin/parking-spots/{id}
    @GetMapping("/parking-spots/{id}")
    public ResponseEntity<Object> getParkingSpotById(@PathVariable("id") String id) {
        return iAdminParkingSpotService.adminGetParkingSpotById(id);
    }



//     Endpoint untuk admin memperbarui data slot parkir.
//     Contoh: PATCH /api/v1/admin/parking-spots/{id}
    @PatchMapping("/parking-spots/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable("id") String id,
                                                    @Valid @RequestBody ParkingSpotUpdateRequest request) {
        return iAdminParkingSpotService.adminUpdateParkingSpot(id, request);
    }



//     Endpoint untuk admin menghapus slot parkir.
//     Contoh: DELETE /api/v1/admin/parking-spots/{id}
    @DeleteMapping("/parking-spots/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable("id") String id) {
        return iAdminParkingSpotService.adminDeleteParkingSpot(id);
    }



//     Endpoint untuk admin melihat semua slot parkir yang terkait dengan merchant tertentu.
//     Contoh: GET /api/v1/admin/parking-spots/by-merchant/MERCH001
    @GetMapping("/parking-spots/by-merchant/{merchantIdentifier}")
    public ResponseEntity<Object> getParkingSpotsByMerchant(@PathVariable("merchantIdentifier") String merchantIdentifier) {
        return iAdminParkingSpotService.adminGetParkingSpotsByMerchant(merchantIdentifier);
    }
}
