package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.entity.ParkingSpot;
import com.soloproject.LegalPark.entity.ParkingSpotStatus;
import com.soloproject.LegalPark.entity.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, String> {

    // Mencari slot parkir berdasarkan nomor slot dan merchant (untuk UniqueConstraint)
    Optional<ParkingSpot> findBySpotNumberAndMerchant(String spotNumber, Merchant merchant);

    // Mencari semua slot parkir berdasarkan merchant
    List<ParkingSpot> findByMerchant(Merchant merchant);

    // Mencari slot parkir yang tersedia di merchant tertentu berdasarkan status AVAILABLE
    List<ParkingSpot> findByMerchantAndStatus(Merchant merchant, ParkingSpotStatus status);

    // Mencari slot parkir yang tersedia di merchant tertentu berdasarkan status dan tipe spot
    List<ParkingSpot> findByMerchantAndStatusAndSpotType(Merchant merchant, ParkingSpotStatus status, SpotType spotType);

    // Opsional: Mencari slot berdasarkan floor dan merchant
    List<ParkingSpot> findByFloorAndMerchant(Integer floor, Merchant merchant);

    // Opsional: Mencari slot berdasarkan ID merchant saja (tanpa perlu fetch objek Merchant)
    List<ParkingSpot> findByMerchant_Id(String merchantId);

    // Opsional: Mencari slot berdasarkan merchant code (jika Anda punya field merchantCode di Merchant)
    // List<ParkingSpot> findByMerchant_MerchantCode(String merchantCode);

    // Mencari semua slot parkir berdasarkan status (misal: semua yang AVAILABLE di semua merchant)
    List<ParkingSpot> findByStatus(ParkingSpotStatus status);

    // Mencari slot parkir berdasarkan spot number (jika masih butuh cek global, tapi hati-hati dengan keunikan)
    // Optional<ParkingSpot> findBySpotNumber(String spotNumber); // Hati-hati jika spotNumber tidak unik secara global
}
