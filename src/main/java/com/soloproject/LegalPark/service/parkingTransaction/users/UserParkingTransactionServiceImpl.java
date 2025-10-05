package com.soloproject.LegalPark.service.parkingTransaction.users;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingEntryRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingExitRequest;
import com.soloproject.LegalPark.dto.response.parkingTransaction.ParkingTransactionResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ParkingTransactionResponseMapper;
import com.soloproject.LegalPark.repository.*;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.payment.IPaymentService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import com.soloproject.LegalPark.util.PaymentResult;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserParkingTransactionServiceImpl implements IUserParkingTransactionService{

    private static final Logger logger = LoggerFactory.getLogger(UserParkingTransactionServiceImpl.class);

    @Autowired
    private ParkingTransactionRepository parkingTransactionRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ParkingSpotRepository parkingSpotRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private IPaymentService paymentService;
    @Autowired
    ParkingTransactionResponseMapper parkingTransactionResponseMapper;

    // Inject NotificationService and TemplateService
    private final INotificationService iNotificationService;
    private final ITemplateService iTemplateService;


    public UserParkingTransactionServiceImpl(INotificationService iNotificationService, ITemplateService iTemplateService) {
        this.iNotificationService = iNotificationService;
        this.iTemplateService = iTemplateService;
    }


    @Override
    public ResponseEntity<Object> recordParkingEntry(ParkingEntryRequest request) {
        // 1. Search Vehicle
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByLicensePlate(request.getLicensePlate());
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle with license plate '" + request.getLicensePlate() + "' not registered.");
        }
        Vehicle vehicle = vehicleOptional.get();

        // 2. Search Merchants
        Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(request.getMerchantCode());
        if (merchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + request.getMerchantCode());
        }
        Merchant merchant = merchantOptional.get();

        // 3. Check whether the vehicle already has active transactions
        Optional<ParkingTransaction> activeTransaction = parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        if (activeTransaction.isPresent()) {
            return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Vehicle '" + request.getLicensePlate() + "' already has an active parking session.");
        }

        // 4. Parking Space Allocation
        ParkingSpot parkingSpot;
        if (request.getSpotNumber() != null && !request.getSpotNumber().isEmpty()) {
            // Try to allocate a specific spot
            Optional<ParkingSpot> specificSpot = parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant);
            if (specificSpot.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking spot '" + request.getSpotNumber() + "' not found at merchant '" + request.getMerchantCode() + "'.");
            }
            parkingSpot = specificSpot.get();
            if (parkingSpot.getStatus() != ParkingSpotStatus.AVAILABLE) {
                return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Parking spot '" + request.getSpotNumber() + "' is not available. Current status: " + parkingSpot.getStatus().name());
            }
        } else {
            // Automatically allocate available spots
            List<ParkingSpot> availableSpots = parkingSpotRepository.findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE);
            if (availableSpots.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.SERVICE_UNAVAILABLE, "FAILED", "No available parking spots at merchant '" + request.getMerchantCode() + "'.");
            }
            parkingSpot = availableSpots.get(0); // Take the first available spot
        }

        // 5. Update Parking Space Status
        parkingSpot.setStatus(ParkingSpotStatus.OCCUPIED);
        parkingSpotRepository.save(parkingSpot);

        // 6. Create a New Parking Transaction
        ParkingTransaction newTransaction = new ParkingTransaction();
        newTransaction.setVehicle(vehicle);
        newTransaction.setParkingSpot(parkingSpot);
        newTransaction.setEntryTime(LocalDateTime.now());
        newTransaction.setStatus(ParkingStatus.ACTIVE); 
        newTransaction.setPaymentStatus(PaymentStatus.PENDING);

        ParkingTransaction savedTransaction = parkingTransactionRepository.save(newTransaction);

        ParkingTransactionResponse response = parkingTransactionResponseMapper.mapToParkingTransactionResponse(savedTransaction);

        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> recordParkingExit(ParkingExitRequest request) {
        // Search for Vehicles
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByLicensePlate(request.getLicensePlate());
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle with license plate '" + request.getLicensePlate() + "' not registered.");
        }
        Vehicle vehicle = vehicleOptional.get();

        // Search for Merchant
        Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(request.getMerchantCode());
        if (merchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + request.getMerchantCode());
        }
        Merchant merchant = merchantOptional.get();

        // Search for active transactions for this vehicle at the same merchant
        Optional<ParkingTransaction> activeTransactionOptional = parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        if (activeTransactionOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "No active parking session found for vehicle '" + request.getLicensePlate() + "'.");
        }
        ParkingTransaction activeTransaction = activeTransactionOptional.get();

        // Verify that the active transaction is indeed at the same merchant (optional, but good for validation)
        if (!activeTransaction.getParkingSpot().getMerchant().getMerchantCode().equals(request.getMerchantCode())) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Active parking session for this vehicle is not at the specified merchant.");
        }


        // Update Waktu Keluar & Hitung Durasi
        activeTransaction.setExitTime(LocalDateTime.now());
//        Duration duration = Duration.between(activeTransaction.getEntryTime(), activeTransaction.getExitTime());
//        long durationMinutes = duration.toMinutes();

        // Hitung Biaya Parkir (Simulasi Sederhana)
        // Ini akan sangat bergantung pada model pricing Anda (misal: tarif per jam merchant)
//        BigDecimal hourlyRate = new BigDecimal("5000"); // Contoh: Rp 5.000 per jam
//        long hours = (durationMinutes + 59) / 60; // Pembulatan ke atas per jam
//        BigDecimal totalCost = hourlyRate.multiply(BigDecimal.valueOf(hours));
//        activeTransaction.setTotalCost(totalCost);

        // Hitung Biaya Parkir menggunakan metode baru
        BigDecimal totalCost = calculateParkingCost(activeTransaction); // <-- Panggil metode baru
        activeTransaction.setTotalCost(totalCost);


        // Proses Pembayaran via PaymentService (Metode baru)
        if (vehicle.getOwner() == null || vehicle.getOwner().getId() == null) {
//            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED", "Vehicle owner information missing for payment. Cannot process payment.");
            
            throw new RuntimeException("Vehicle owner information missing for payment. Cannot process payment.");
        }

        // NEW CALL TO PAYMENTSERVICE WITH THE verificationCode PARAMETER
        PaymentResult paymentResult = paymentService.processParkingPayment(
                vehicle.getOwner().getId(),
                totalCost,
                activeTransaction.getId(),
                request.getVerificationCode()
        );

        // 8. Based on the payment results, update the parking transaction status.
        if (paymentResult == PaymentResult.SUCCESS) {
            activeTransaction.setPaymentStatus(PaymentStatus.PAID);
            activeTransaction.setStatus(ParkingStatus.COMPLETED);

            // 9. Update Parking Space Status
            ParkingSpot parkingSpot = activeTransaction.getParkingSpot();
            if (parkingSpot != null) {
                parkingSpot.setStatus(ParkingSpotStatus.AVAILABLE);
                parkingSpotRepository.save(parkingSpot);
            }

            // 10. Save Updated Transactions
            ParkingTransaction updatedTransaction = parkingTransactionRepository.save(activeTransaction);

            try { // START OF EMAIL DELIVERY
                Map<String, Object> templateVariables = new HashMap<>();
                templateVariables.put("name", vehicle.getOwner().getAccountName());
                templateVariables.put("licensePlate", updatedTransaction.getVehicle().getLicensePlate());
                templateVariables.put("merchantName", updatedTransaction.getParkingSpot().getMerchant().getMerchantName());
                templateVariables.put("entryTime", updatedTransaction.getEntryTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
                templateVariables.put("exitTime", updatedTransaction.getExitTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
                templateVariables.put("totalCost", "Rp " + updatedTransaction.getTotalCost().toPlainString()); // Format mata uang


                String emailBody = iTemplateService.processEmailTemplate("payment_success_confirmation", templateVariables);

                EmailNotificationRequest emailRequest = new EmailNotificationRequest();
                emailRequest.setTo(vehicle.getOwner().getEmail());
                emailRequest.setSubject("LegalPark - Konfirmasi Pembayaran Parkir Berhasil!");
                emailRequest.setBody(emailBody);
                iNotificationService.sendEmailNotification(emailRequest);

                logger.info("Payment success confirmation email sent to user {}: {}", vehicle.getOwner().getId(), vehicle.getOwner().getEmail());
            } catch (Exception emailEx) {
                logger.error("Failed to send payment confirmation email to user {}: {}", vehicle.getOwner().getId(), emailEx.getMessage(), emailEx);

            }  // END OF EMAIL DELIVERY


            String successMessage = "Vehicle exited successfully. Payment successful. Total cost: Rp " + totalCost.toPlainString();
            return ResponseHandler.generateResponseSuccess(parkingTransactionResponseMapper.mapToParkingTransactionResponse(updatedTransaction));

        } else if (paymentResult == PaymentResult.INSUFFICIENT_BALANCE) {
            activeTransaction.setPaymentStatus(PaymentStatus.FAILED); // Payment failed due to insufficient balance

            parkingTransactionRepository.save(activeTransaction);

            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Insufficient balance. Total cost: Rp " + totalCost.toPlainString() + ".");

        } else {
            activeTransaction.setPaymentStatus(PaymentStatus.FAILED);

            parkingTransactionRepository.save(activeTransaction);

            String errorMessage = "Payment failed. Please check your verification code and try again.";

            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED", errorMessage);
        }
    }

    /**
     * Calculate the total parking cost based on duration.
     */
    private BigDecimal calculateParkingCost(ParkingTransaction transaction) {
        if (transaction.getEntryTime() == null || transaction.getExitTime() == null) {

            return BigDecimal.ZERO;
        }

        Duration duration = Duration.between(transaction.getEntryTime(), transaction.getExitTime());
        long totalMinutes = duration.toMinutes();

        // Contoh tarif sederhana: Rp 5.000 per jam, pembulatan ke atas.
        // Jika durasi 0-60 menit -> 1 jam
        // Jika durasi 61-120 menit -> 2 jam, dst.
        BigDecimal hourlyRate = new BigDecimal("5000"); // Tarif dasar per jam

        long hours = (totalMinutes + 59) / 60; // Pembulatan ke atas
        if (hours == 0 && totalMinutes > 0) { // Pastikan minimal 1 jam jika durasi bukan 0
            hours = 1;
        } else if (totalMinutes == 0) { // Jika durasi 0, biaya 0
            hours = 0;
        }

        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }

    @Override
    public ResponseEntity<Object> getUserActiveParkingTransaction(String licensePlate) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByLicensePlate(licensePlate);
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle with license plate '" + licensePlate + "' not registered.");
        }
        Vehicle vehicle = vehicleOptional.get();

        Optional<ParkingTransaction> activeTransaction = parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        if (activeTransaction.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "SUCCESS", "No active parking session found for vehicle '" + licensePlate + "'.");
        }
        return ResponseHandler.generateResponseSuccess(parkingTransactionResponseMapper.mapToParkingTransactionResponse(activeTransaction.get()));
    }

    @Override
    public ResponseEntity<Object> getUserParkingTransactionHistory(String licensePlate) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByLicensePlate(licensePlate);
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle with license plate '" + licensePlate + "' not registered.");
        }
        Vehicle vehicle = vehicleOptional.get();

        List<ParkingTransaction> transactions = parkingTransactionRepository.findByVehicle(vehicle);
        List<ParkingTransactionResponse> responses = transactions.stream()
                .map(parkingTransactionResponseMapper::mapToParkingTransactionResponse)
                .collect(Collectors.toList());

        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> getUserParkingTransactionDetails(String transactionId, String licensePlate) {
        Optional<ParkingTransaction> transactionOptional = parkingTransactionRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking transaction not found with ID: " + transactionId);
        }
        ParkingTransaction transaction = transactionOptional.get();

        // Security validation: Ensure that this transaction belongs to the vehicle with the specified license plate number.
        if (!transaction.getVehicle().getLicensePlate().equals(licensePlate)) {
            return ResponseHandler.generateResponseError(HttpStatus.FORBIDDEN, "FAILED", "Access denied. This transaction does not belong to the specified vehicle.");
        }

        return ResponseHandler.generateResponseSuccess(parkingTransactionResponseMapper.mapToParkingTransactionResponse(transaction));
    }
}
