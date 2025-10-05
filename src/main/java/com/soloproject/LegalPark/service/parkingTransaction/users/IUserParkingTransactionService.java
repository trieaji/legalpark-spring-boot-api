package com.soloproject.LegalPark.service.parkingTransaction.users;

import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingEntryRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingExitRequest;
import org.springframework.http.ResponseEntity;

public interface IUserParkingTransactionService {
    /**
     * [USER] Recording the entry of vehicles into parking slots.
     * It will search for vehicles and parking slots, then change the slot status to OCCUPIED.
     * Create a new parking transaction entry with ACTIVE status and PENDING payment.
     */
    ResponseEntity<Object> recordParkingEntry(ParkingEntryRequest request);



    /**
     * [USER] Recording vehicles leaving parking spaces and processing payments.
     * Search for active transactions based on license plate numbers, calculate fees, verify payments (e.g., with codes).
     * Change the slot status to AVAILABLE, and update the transaction status.
     */
    ResponseEntity<Object> recordParkingExit(ParkingExitRequest request);



    /**
     * [USER] Retrieve active parking transactions (ACTIVE status) for the user's vehicle license plate number.
     */
    ResponseEntity<Object> getUserActiveParkingTransaction(String licensePlate);



    /**
     * [USER] Retrieve the history of all parking transactions associated with the user's vehicle license plate number.
     */
    ResponseEntity<Object> getUserParkingTransactionHistory(String licensePlate);



    /**
     * [USER] Retrieve specific parking transaction details based on the transaction ID.
     */
    ResponseEntity<Object> getUserParkingTransactionDetails(String transactionId, String licensePlate);
}
