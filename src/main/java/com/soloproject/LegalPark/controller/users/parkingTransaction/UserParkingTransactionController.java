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
@RequestMapping("/api/v1/user")
@Tag(name = "User Parking Transaction API", description = "Endpoint untuk Pengguna mengelola transaksi parkir mereka")
public class UserParkingTransactionController {
    @Autowired
    private IUserParkingTransactionService iUserParkingTransactionService;




    @PostMapping("/parking-transactions/entry")
    public ResponseEntity<Object> recordParkingEntry(@Valid @RequestBody ParkingEntryRequest request) {
        return iUserParkingTransactionService.recordParkingEntry(request);
    }




    @PostMapping("/parking-transactions/exit")
    public ResponseEntity<Object> recordParkingExit(@Valid @RequestBody ParkingExitRequest request) {
        return iUserParkingTransactionService.recordParkingExit(request);
    }




    @GetMapping("/parking-transactions/active")
    public ResponseEntity<Object> getUserActiveParkingTransaction(@RequestParam("licensePlate") String licensePlate) {
        return iUserParkingTransactionService.getUserActiveParkingTransaction(licensePlate);
    }




    @GetMapping("/parking-transactions/history")
    public ResponseEntity<Object> getUserParkingTransactionHistory(@RequestParam("licensePlate") String licensePlate) {
        return iUserParkingTransactionService.getUserParkingTransactionHistory(licensePlate);
    }



    @GetMapping("/parking-transactions/details/{transactionId}")
    public ResponseEntity<Object> getUserParkingTransactionDetails(@PathVariable("transactionId") String transactionId,
                                                                   @RequestParam("licensePlate") String licensePlate) {
        return iUserParkingTransactionService.getUserParkingTransactionDetails(transactionId, licensePlate);
    }
}
