package com.soloproject.LegalPark.controller.users.parkingTransaction;

import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingEntryRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingExitRequest;
import com.soloproject.LegalPark.service.parkingTransaction.users.IUserParkingTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user") // Base path untuk semua endpoint user
@Tag(name = "User Parking Transaction API", description = "Endpoint untuk Pengguna mengelola transaksi parkir mereka")
public class UserParkingTransactionController {
    @Autowired
    private IUserParkingTransactionService iUserParkingTransactionService;



//     Endpoint untuk pengguna mencatat masuknya kendaraan ke slot parkir.
//     Contoh: POST /api/v1/user/parking-transactions/entry
//     @Valid Memastikan DTO ParkingEntryRequest dan ParkingExitRequest divalidasi secara otomatis berdasarkan anotasi validasi (@NotBlank, @Size) yang Anda letakkan di dalamnya.
    @PostMapping("/parking-transactions/entry")
    public ResponseEntity<Object> recordParkingEntry(@Valid @RequestBody ParkingEntryRequest request) {
        return iUserParkingTransactionService.recordParkingEntry(request);
    }



//     Endpoint untuk pengguna mencatat keluarnya kendaraan dari slot parkir dan memproses pembayaran.
//     Contoh: POST /api/v1/user/parking-transactions/exit
    @PostMapping("/parking-transactions/exit")
    public ResponseEntity<Object> recordParkingExit(@Valid @RequestBody ParkingExitRequest request) {
        return iUserParkingTransactionService.recordParkingExit(request);
    }



//     Endpoint untuk pengguna melihat transaksi parkir aktif mereka.
//     Contoh: GET /api/v1/user/parking-transactions/active?licensePlate=B1234XYZ
    @GetMapping("/parking-transactions/active")
    public ResponseEntity<Object> getUserActiveParkingTransaction(@RequestParam("licensePlate") String licensePlate) {
        return iUserParkingTransactionService.getUserActiveParkingTransaction(licensePlate);
    }



//     Endpoint untuk pengguna melihat riwayat transaksi parkir mereka.
//     Contoh: GET /api/v1/user/parking-transactions/history?licensePlate=B1234XYZ
    @GetMapping("/parking-transactions/history")
    public ResponseEntity<Object> getUserParkingTransactionHistory(@RequestParam("licensePlate") String licensePlate) {
        return iUserParkingTransactionService.getUserParkingTransactionHistory(licensePlate);
    }


//     Endpoint untuk pengguna melihat detail transaksi parkir tertentu.
//     Contoh: GET /api/v1/user/parking-transactions/details/{transactionId}?licensePlate=B1234XYZ
    @GetMapping("/parking-transactions/details/{transactionId}")
    public ResponseEntity<Object> getUserParkingTransactionDetails(@PathVariable("transactionId") String transactionId,
                                                                   @RequestParam("licensePlate") String licensePlate) {
        return iUserParkingTransactionService.getUserParkingTransactionDetails(transactionId, licensePlate);
    }
}
