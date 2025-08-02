package com.soloproject.LegalPark.controller.admin.parkingTransaction;

import com.soloproject.LegalPark.entity.ParkingStatus;
import com.soloproject.LegalPark.entity.PaymentStatus;
import com.soloproject.LegalPark.service.parkingTransaction.admin.IAdminParkingTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin") // Base path untuk semua endpoint admin
@Tag(name = "Admin Parking Transaction API", description = "Endpoint untuk Admin mengelola dan melihat transaksi parkir")
public class AdminParkingTransactionController {
    @Autowired
    private IAdminParkingTransactionService iAdminParkingTransactionService;



//     Endpoint untuk admin melihat semua transaksi parkir yang terdaftar.
//     Contoh: GET /api/v1/admin/parking-transactions
    @GetMapping("/parking-transactions")
    public ResponseEntity<Object> getAllParkingTransactions() {
        return iAdminParkingTransactionService.adminGetAllParkingTransactions();
    }



//     Endpoint untuk admin melihat detail transaksi parkir berdasarkan ID.
//     Contoh: GET /api/v1/admin/parking-transactions/{transactionId}
    @GetMapping("/parking-transactions/{transactionId}")
    public ResponseEntity<Object> getParkingTransactionById(@PathVariable("transactionId") String transactionId) {
        return iAdminParkingTransactionService.adminGetParkingTransactionById(transactionId);
    }



//     Endpoint untuk admin melihat semua transaksi parkir yang terkait dengan ID kendaraan tertentu.
//     Contoh: GET /api/v1/admin/parking-transactions/by-vehicle/{vehicleId}
    @GetMapping("/parking-transactions/by-vehicle/{vehicleId}")
    public ResponseEntity<Object> getParkingTransactionsByVehicleId(@PathVariable("vehicleId") String vehicleId) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByVehicleId(vehicleId);
    }



//     Endpoint untuk admin melihat semua transaksi parkir yang terkait dengan ID slot parkir tertentu.
//     Contoh: GET /api/v1/admin/parking-transactions/by-spot/{parkingSpotId}
    @GetMapping("/parking-transactions/by-spot/{parkingSpotId}")
    public ResponseEntity<Object> getParkingTransactionsByParkingSpotId(@PathVariable("parkingSpotId") String parkingSpotId) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByParkingSpotId(parkingSpotId);
    }


    
//     Endpoint untuk admin melihat semua transaksi parkir yang terkait dengan ID merchant tertentu.
//     Contoh: GET /api/v1/admin/parking-transactions/by-merchant/{merchantId}
    @GetMapping("/parking-transactions/by-merchant/{merchantId}")
    public ResponseEntity<Object> getParkingTransactionsByMerchantId(@PathVariable("merchantId") String merchantId) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByMerchantId(merchantId);
    }



//     Endpoint untuk admin melihat semua transaksi parkir berdasarkan status parkir.
//     Contoh: GET /api/v1/admin/parking-transactions/by-parking-status?status=ACTIVE
    @GetMapping("/parking-transactions/by-parking-status")
    public ResponseEntity<Object> getParkingTransactionsByParkingStatus(@RequestParam("status") ParkingStatus status) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByParkingStatus(status);
    }



//     Endpoint untuk admin melihat semua transaksi parkir berdasarkan status pembayaran.
//     Contoh: GET /api/v1/admin/parking-transactions/by-payment-status?status=PENDING
    @GetMapping("/parking-transactions/by-payment-status")
    public ResponseEntity<Object> getParkingTransactionsByPaymentStatus(@RequestParam("status") PaymentStatus status) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByPaymentStatus(status);
    }



//     Endpoint untuk admin memperbarui status pembayaran transaksi secara manual.
//     Contoh: PATCH /api/v1/admin/parking-transactions/{transactionId}/payment-status?newPaymentStatus=PAID
    @PatchMapping("/parking-transactions/{transactionId}/payment-status")
    public ResponseEntity<Object> updateParkingTransactionPaymentStatus(@PathVariable("transactionId") String transactionId,
                                                                        @RequestParam("newPaymentStatus") PaymentStatus newPaymentStatus) {
        return iAdminParkingTransactionService.adminUpdateParkingTransactionPaymentStatus(transactionId, newPaymentStatus);
    }



//     Endpoint untuk admin membatalkan transaksi parkir.
//     Contoh: PATCH /api/v1/admin/parking-transactions/{transactionId}/cancel
    @PatchMapping("/parking-transactions/{transactionId}/cancel")
    public ResponseEntity<Object> cancelParkingTransaction(@PathVariable("transactionId") String transactionId) {
        return iAdminParkingTransactionService.adminCancelParkingTransaction(transactionId);
    }
}
