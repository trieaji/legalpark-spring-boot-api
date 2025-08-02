package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingTransactionRepository extends JpaRepository<ParkingTransaction, String> {
    // Mencari transaksi parkir berdasarkan status (misal: semua yang ACTIVE)
    List<ParkingTransaction> findByStatus(ParkingStatus status);

    // Mencari transaksi parkir berdasarkan status pembayaran (misal: semua yang PENDING)
    List<ParkingTransaction> findByPaymentStatus(PaymentStatus paymentStatus);

    // Mencari transaksi berdasarkan kendaraan
    List<ParkingTransaction> findByVehicle(Vehicle vehicle);

    // Mencari transaksi berdasarkan slot parkir
    List<ParkingTransaction> findByParkingSpot(ParkingSpot parkingSpot);

    // Mencari transaksi aktif (IN_PROGRESS/ACTIVE) untuk kendaraan tertentu
    // Ini penting untuk memastikan satu kendaraan tidak memiliki dua transaksi aktif sekaligus
    Optional<ParkingTransaction> findByVehicleAndStatus(Vehicle vehicle, ParkingStatus status);

    // Mencari transaksi aktif (IN_PROGRESS/ACTIVE) di slot parkir tertentu
    // Optional<ParkingTransaction> findByParkingSpotAndStatus(ParkingSpot parkingSpot, ParkingStatus status);

    // Mencari transaksi berdasarkan ID kendaraan (tanpa perlu fetch objek Vehicle)
    List<ParkingTransaction> findByVehicle_Id(String vehicleId);

    // JPA akan secara otomatis memahami navigasi melalui relasi:
    // ParkingTransaction -> ParkingSpot (melalui field 'parkingSpot')
    // ParkingSpot -> Merchant (melalui field 'merchant')
    List<ParkingTransaction> findByParkingSpot_Merchant(Merchant merchant);



//     Mengambil daftar transaksi parkir berdasarkan ID pemilik kendaraan (user) dan rentang waktu masuk.
//     Melakukan JOIN dari ParkingTransaction -> Vehicle -> Users.
    @Query("SELECT pt FROM ParkingTransaction pt JOIN pt.vehicle v JOIN v.owner u WHERE u.id = :userId AND pt.entryTime BETWEEN :startDateTime AND :endDateTime ORDER BY pt.entryTime DESC")
    List<ParkingTransaction> findByVehicleOwnerIdAndEntryTimeBetween(@Param("userId") String userId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);


//     Mengambil semua transaksi parkir yang terkait dengan kendaraan milik user tertentu.
//     Melakukan JOIN dari ParkingTransaction -> Vehicle -> Users.
    @Query("SELECT pt FROM ParkingTransaction pt JOIN pt.vehicle v JOIN v.owner u WHERE u.id = :userId ORDER BY pt.entryTime DESC")
    List<ParkingTransaction> findByVehicleOwnerId(@Param("userId") String userId);


//     Mengambil transaksi pembayaran yang berstatus 'PAID' dalam rentang waktu keluar tertentu.
//     Digunakan untuk laporan pendapatan harian (admin).
    @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.paymentStatus = 'PAID' AND pt.exitTime BETWEEN :startOfDay AND :endOfDay ORDER BY pt.exitTime DESC")
    List<ParkingTransaction> findPaidTransactionsByExitTimeBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);


//     Mengambil transaksi pembayaran yang berstatus 'PAID' dalam rentang waktu keluar tertentu
//     dan di merchant spesifik. Melakukan JOIN dari ParkingTransaction -> ParkingSpot -> Merchant.
    @Query("SELECT pt FROM ParkingTransaction pt JOIN pt.parkingSpot ps JOIN ps.merchant m WHERE pt.paymentStatus = 'PAID' AND pt.exitTime BETWEEN :startOfDay AND :endOfDay AND m.merchantCode = :merchantCode ORDER BY pt.exitTime DESC")
    List<ParkingTransaction> findPaidTransactionsByExitTimeBetweenAndMerchantCode(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("merchantCode") String merchantCode);


//     Mengambil transaksi parkir aktif yang sedang menempati slot parkir tertentu.
//     Digunakan untuk laporan hunian slot parkir (admin).
//     Melakukan JOIN implisit karena pt.parkingSpot sudah merupakan objek ParkingSpot.
    @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.parkingSpot = :parkingSpot AND pt.status = :status")
    Optional<ParkingTransaction> findByParkingSpotAndStatus(@Param("parkingSpot") ParkingSpot parkingSpot, @Param("status") ParkingStatus status);


}
