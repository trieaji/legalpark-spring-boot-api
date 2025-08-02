package com.soloproject.LegalPark.service.parkingSpot.admin;

import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotRequest;
import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotUpdateRequest;
import org.springframework.http.ResponseEntity;

public interface IAdminParkingSpotService { //menangani di mana kendaraan itu parkir (slot parkir)
    // 1. Mendaftarkan Slot Parkir Baru (CREATE)
    ResponseEntity<Object> adminCreateParkingSpot(ParkingSpotRequest request);

    // 2. Melihat Semua Slot Parkir (GET ALL)
    ResponseEntity<Object> adminGetAllParkingSpots();

    // 3. Melihat Detail Slot Parkir Tertentu berdasarkan ID (GET by ID)
    ResponseEntity<Object> adminGetParkingSpotById(String id);

    // 4. Memperbarui Data Slot Parkir (UPDATE)
    ResponseEntity<Object> adminUpdateParkingSpot(String id, ParkingSpotUpdateRequest request);

    // 5. Menghapus Slot Parkir (DELETE)
    ResponseEntity<Object> adminDeleteParkingSpot(String id);

    // 6. Melihat Slot Parkir berdasarkan Merchant (Opsional, tapi sangat berguna)
    // Parameter bisa berupa merchantId atau merchantCode
    ResponseEntity<Object> adminGetParkingSpotsByMerchant(String merchantIdentifier); // merchantId/merchantCode

    /*
    * Untuk Admin Perlu akses penuh untuk mengelola slot parkir (membuat, memperbarui detail, menghapus, melihat semua).
    * */
}
