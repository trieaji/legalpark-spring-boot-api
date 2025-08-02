package com.soloproject.LegalPark.service.parkingTransaction.admin;

import com.soloproject.LegalPark.entity.ParkingStatus;
import com.soloproject.LegalPark.entity.PaymentStatus;
import org.springframework.http.ResponseEntity;

public interface IAdminParkingTransactionService {

//     [ADMIN] Mengambil semua transaksi parkir yang ada di sistem.
//     @return ResponseEntity berisi daftar semua transaksi parkir.
    ResponseEntity<Object> adminGetAllParkingTransactions();



//     [ADMIN] Mengambil detail transaksi parkir berdasarkan ID transaksi.
//     @return ResponseEntity berisi detail transaksi atau pesan error jika tidak ditemukan.
    ResponseEntity<Object> adminGetParkingTransactionById(String transactionId);



//     [ADMIN] Mengambil semua transaksi parkir yang terkait dengan ID kendaraan tertentu.
//     @return ResponseEntity berisi daftar transaksi parkir kendaraan tersebut.
    ResponseEntity<Object> adminGetParkingTransactionsByVehicleId(String vehicleId);



//     [ADMIN] Mengambil semua transaksi parkir yang terkait dengan ID slot parkir tertentu.
//     @return ResponseEntity berisi daftar transaksi parkir di slot tersebut.
    ResponseEntity<Object> adminGetParkingTransactionsByParkingSpotId(String parkingSpotId);



//     [ADMIN] Mengambil semua transaksi parkir yang terkait dengan ID merchant tertentu.
//     @return ResponseEntity berisi daftar transaksi parkir di merchant tersebut.
    ResponseEntity<Object> adminGetParkingTransactionsByMerchantId(String merchantId);



//     [ADMIN] Mengambil semua transaksi parkir berdasarkan status parkir (ACTIVE, COMPLETED, CANCELLED).
//     @return ResponseEntity berisi daftar transaksi parkir dengan status yang cocok.
    ResponseEntity<Object> adminGetParkingTransactionsByParkingStatus(ParkingStatus status);



//     [ADMIN] Mengambil semua transaksi parkir berdasarkan status pembayaran (PENDING, PAID, FAILED).
//     @return ResponseEntity berisi daftar transaksi parkir dengan status pembayaran yang cocok.
    ResponseEntity<Object> adminGetParkingTransactionsByPaymentStatus(PaymentStatus paymentStatus);



//     [ADMIN] Memperbarui status pembayaran suatu transaksi secara manual.
//     @return ResponseEntity berisi detail transaksi yang diperbarui atau pesan error.
    ResponseEntity<Object> adminUpdateParkingTransactionPaymentStatus(String transactionId, PaymentStatus newPaymentStatus);



//     [ADMIN] Membatalkan transaksi parkir yang sedang aktif.
//     @return ResponseEntity konfirmasi pembatalan atau pesan error.
    ResponseEntity<Object> adminCancelParkingTransaction(String transactionId);
}
