package com.soloproject.LegalPark.controller.admin.parkingTransaction;

import com.soloproject.LegalPark.entity.ParkingStatus;
import com.soloproject.LegalPark.entity.PaymentStatus;
import com.soloproject.LegalPark.service.parkingTransaction.admin.IAdminParkingTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Parking Transaction API", description = "Endpoint untuk Admin mengelola dan melihat transaksi parkir")
public class AdminParkingTransactionController {
    @Autowired
    private IAdminParkingTransactionService iAdminParkingTransactionService;




    @GetMapping("/parking-transactions")
    public ResponseEntity<Object> getAllParkingTransactions() {
        return iAdminParkingTransactionService.adminGetAllParkingTransactions();
    }




    @GetMapping("/parking-transactions/{transactionId}")
    public ResponseEntity<Object> getParkingTransactionById(@PathVariable("transactionId") String transactionId) {
        return iAdminParkingTransactionService.adminGetParkingTransactionById(transactionId);
    }




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


    

    @GetMapping("/parking-transactions/by-merchant/{merchantId}")
    public ResponseEntity<Object> getParkingTransactionsByMerchantId(@PathVariable("merchantId") String merchantId) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByMerchantId(merchantId);
    }




    @GetMapping("/parking-transactions/by-parking-status")
    public ResponseEntity<Object> getParkingTransactionsByParkingStatus(@RequestParam("status") ParkingStatus status) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByParkingStatus(status);
    }




    @GetMapping("/parking-transactions/by-payment-status")
    public ResponseEntity<Object> getParkingTransactionsByPaymentStatus(@RequestParam("status") PaymentStatus status) {
        return iAdminParkingTransactionService.adminGetParkingTransactionsByPaymentStatus(status);
    }




    @PatchMapping("/parking-transactions/{transactionId}/payment-status")
    public ResponseEntity<Object> updateParkingTransactionPaymentStatus(@PathVariable("transactionId") String transactionId,
                                                                        @RequestParam("newPaymentStatus") PaymentStatus newPaymentStatus) {
        return iAdminParkingTransactionService.adminUpdateParkingTransactionPaymentStatus(transactionId, newPaymentStatus);
    }




    @PatchMapping("/parking-transactions/{transactionId}/cancel")
    public ResponseEntity<Object> cancelParkingTransaction(@PathVariable("transactionId") String transactionId) {
        return iAdminParkingTransactionService.adminCancelParkingTransaction(transactionId);
    }
}
