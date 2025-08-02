package com.soloproject.LegalPark.service.parkingTransaction.users;

import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingEntryRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingExitRequest;
import org.springframework.http.ResponseEntity;

public interface IUserParkingTransactionService {
    /**
     * [USER] Mencatat masuknya kendaraan ke slot parkir.
     * Akan mencari kendaraan dan slot parkir, lalu mengubah status slot menjadi OCCUPIED.
     * Membuat entri transaksi parkir baru dengan status ACTIVE dan PENDING pembayaran.
     */
    ResponseEntity<Object> recordParkingEntry(ParkingEntryRequest request);



    /**
     * [USER] Mencatat keluarnya kendaraan dari slot parkir dan memproses pembayaran.
     * Akan mencari transaksi aktif berdasarkan plat nomor, menghitung biaya, memverifikasi pembayaran (misal dengan kode),
     * mengubah status slot menjadi AVAILABLE, dan mengupdate status transaksi.
     */
    ResponseEntity<Object> recordParkingExit(ParkingExitRequest request);



    /**
     * [USER] Mengambil transaksi parkir yang sedang aktif (status ACTIVE) untuk plat nomor kendaraan pengguna.
     */
    ResponseEntity<Object> getUserActiveParkingTransaction(String licensePlate);



    /**
     * [USER] Mengambil riwayat semua transaksi parkir yang terkait dengan plat nomor kendaraan pengguna.
     */
    ResponseEntity<Object> getUserParkingTransactionHistory(String licensePlate);



    /**
     * [USER] Mengambil detail transaksi parkir tertentu berdasarkan ID transaksi.
     */
    ResponseEntity<Object> getUserParkingTransactionDetails(String transactionId, String licensePlate);
}
