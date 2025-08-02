package com.soloproject.LegalPark.service.parkingTransaction.admin;

import com.soloproject.LegalPark.dto.response.parkingTransaction.ParkingTransactionResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ParkingTransactionResponseMapper;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.ParkingSpotRepository;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminParkingTransactionServiceImpl implements IAdminParkingTransactionService{

    @Autowired
    private ParkingTransactionRepository parkingTransactionRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ParkingSpotRepository parkingSpotRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    ParkingTransactionResponseMapper parkingTransactionResponseMapper;

    
    @Override
    public ResponseEntity<Object> adminGetAllParkingTransactions() {
        List<ParkingTransaction> transactions = parkingTransactionRepository.findAll();
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingTransactionById(String transactionId) {
        Optional<ParkingTransaction> transactionOptional = parkingTransactionRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking transaction not found with ID: " + transactionId);
        }
        return ResponseHandler.generateResponseSuccess(parkingTransactionResponseMapper.mapToParkingTransactionResponse(transactionOptional.get()));
    }

    @Override
    public ResponseEntity<Object> adminGetParkingTransactionsByVehicleId(String vehicleId) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(vehicleId);
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle not found with ID: " + vehicleId);
        }
        List<ParkingTransaction> transactions = parkingTransactionRepository.findByVehicle(vehicleOptional.get());
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingTransactionsByParkingSpotId(String parkingSpotId) {
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotRepository.findById(parkingSpotId);
        if (parkingSpotOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking spot not found with ID: " + parkingSpotId);
        }
        List<ParkingTransaction> transactions = parkingTransactionRepository.findByParkingSpot(parkingSpotOptional.get());
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingTransactionsByMerchantId(String merchantId) {
        Optional<Merchant> merchantOptional = merchantRepository.findById(merchantId); // Asumsi merchantId adalah ID DB
        if (merchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with ID: " + merchantId);
        }
        // Mencari transaksi berdasarkan merchant melalui parking spot
        List<ParkingTransaction> transactions = parkingTransactionRepository.findByParkingSpot_Merchant(merchantOptional.get());
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingTransactionsByParkingStatus(ParkingStatus status) {
        List<ParkingTransaction> transactions = parkingTransactionRepository.findByStatus(status);
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingTransactionsByPaymentStatus(PaymentStatus paymentStatus) {
        List<ParkingTransaction> transactions = parkingTransactionRepository.findByPaymentStatus(paymentStatus);
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminUpdateParkingTransactionPaymentStatus(String transactionId, PaymentStatus newPaymentStatus) {
        Optional<ParkingTransaction> transactionOptional = parkingTransactionRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking transaction not found with ID: " + transactionId);
        }
        ParkingTransaction transaction = transactionOptional.get();

        // Anda bisa menambahkan validasi di sini, misalnya tidak boleh mengubah status ke PAID jika sudah FAILED kecuali ada mekanisme retry
        transaction.setPaymentStatus(newPaymentStatus);
        ParkingTransaction updatedTransaction = parkingTransactionRepository.save(transaction);

        return ResponseHandler.generateResponseSuccess(parkingTransactionResponseMapper.mapToParkingTransactionResponse(updatedTransaction));
    }

    @Override
    public ResponseEntity<Object> adminCancelParkingTransaction(String transactionId) {
        Optional<ParkingTransaction> transactionOptional = parkingTransactionRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking transaction not found with ID: " + transactionId);
        }
        ParkingTransaction transaction = transactionOptional.get();

        // Validasi: Hanya transaksi ACTIVE atau PENDING yang bisa dibatalkan secara manual
        if (transaction.getStatus() == ParkingStatus.COMPLETED) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Completed transactions cannot be cancelled.");
        }

        // Untuk tujuan pembatalan, fokus ke ParkingStatus
        if (transaction.getStatus() == ParkingStatus.CANCELLED) {
            return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Transaction is already cancelled.");
        }


        // Jika transaksi ACTIVE, kembalikan status slot parkir menjadi AVAILABLE
        if (transaction.getStatus() == ParkingStatus.ACTIVE) {
            ParkingSpot parkingSpot = transaction.getParkingSpot();
            if (parkingSpot != null) {
                parkingSpot.setStatus(ParkingSpotStatus.AVAILABLE);
                parkingSpotRepository.save(parkingSpot);
            }
        }

        transaction.setStatus(ParkingStatus.CANCELLED);
        // Opsi: Jika dibatalkan, status pembayaran juga bisa disetel ke FAILED atau CANCELLED (jika ada enum CANCELLED di PaymentStatus)
        // transaction.setPaymentStatus(PaymentStatus.FAILED);
        parkingTransactionRepository.save(transaction);

        return ResponseHandler.generateResponseSuccess(null);
    }
}
