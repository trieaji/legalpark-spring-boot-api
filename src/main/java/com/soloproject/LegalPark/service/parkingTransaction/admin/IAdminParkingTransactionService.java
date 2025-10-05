package com.soloproject.LegalPark.service.parkingTransaction.admin;

import com.soloproject.LegalPark.entity.ParkingStatus;
import com.soloproject.LegalPark.entity.PaymentStatus;
import org.springframework.http.ResponseEntity;

public interface IAdminParkingTransactionService {

//     [ADMIN] Retrieve all parking transactions in the system.
    ResponseEntity<Object> adminGetAllParkingTransactions();



//     [ADMIN] Retrieve parking transaction details based on transaction ID.
    ResponseEntity<Object> adminGetParkingTransactionById(String transactionId);



//     [ADMIN] Retrieve all parking transactions associated with a specific vehicle ID.
    ResponseEntity<Object> adminGetParkingTransactionsByVehicleId(String vehicleId);



//     [ADMIN] Retrieve all parking transactions associated with a specific parking slot ID.
    ResponseEntity<Object> adminGetParkingTransactionsByParkingSpotId(String parkingSpotId);



//     [ADMIN] Retrieve all parking transactions associated with a specific merchant ID.
    ResponseEntity<Object> adminGetParkingTransactionsByMerchantId(String merchantId);



//     [ADMIN] Retrieve all parking transactions based on parking status (ACTIVE, COMPLETED, CANCELLED).
    ResponseEntity<Object> adminGetParkingTransactionsByParkingStatus(ParkingStatus status);



//     [ADMIN] Retrieve all parking transactions based on payment status (PENDING, PAID, FAILED).
    ResponseEntity<Object> adminGetParkingTransactionsByPaymentStatus(PaymentStatus paymentStatus);



//     [ADMIN] Manually updating the payment status of a transaction.
    ResponseEntity<Object> adminUpdateParkingTransactionPaymentStatus(String transactionId, PaymentStatus newPaymentStatus);



//     [ADMIN] Cancel an active parking transaction.
    ResponseEntity<Object> adminCancelParkingTransaction(String transactionId);
}
