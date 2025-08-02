package com.soloproject.LegalPark.service.verificationCode;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.PaymentVerificationCodeRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.VerifyPaymentCodeRequest;
import com.soloproject.LegalPark.entity.ParkingStatus;
import com.soloproject.LegalPark.entity.ParkingTransaction;
import com.soloproject.LegalPark.entity.PaymentVerificationCode;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.GenerateOtp;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.repository.PaymentVerificationCodeRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class VerificationCodeServiceImpl implements IVerificationCodeService{

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ParkingTransactionRepository parkingTransactionRepository;

    @Autowired
    private PaymentVerificationCodeRepository paymentVerificationCodeRepository;


    @Autowired
    private INotificationService notificationService;

    @Autowired
    private ITemplateService templateService;

    private boolean isCodeExpired(LocalDateTime expiryDate, LocalDateTime currentTime) {
        return currentTime.isAfter(expiryDate);
    }

    @Override
    public ResponseEntity<Object> generateAndSendPaymentVerificationCode(PaymentVerificationCodeRequest request) {
        try {
            Optional<Users> userOptional = usersRepository.findById(request.getUserId());
            if (userOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found.");
            }
            Users user = userOptional.get();

            // 1. Cari transaksi parkir yang aktif berdasarkan parkingTransactionId
            Optional<ParkingTransaction> ptOptional = parkingTransactionRepository.findById(request.getParkingTransactionId());
            if (ptOptional.isEmpty() || ptOptional.get().getStatus() != ParkingStatus.ACTIVE) {
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Active parking transaction not found or invalid for ID: " + request.getParkingTransactionId());
            }
            ParkingTransaction parkingTransaction = ptOptional.get();

            String otpCode = GenerateOtp.generateRandomNumber();
            LocalDateTime expiryDate = GenerateOtp.getExpiryDate();


            PaymentVerificationCode paymentCode = new PaymentVerificationCode();
            paymentCode.setUser(user);
            paymentCode.setCode(otpCode);
            paymentCode.setCreatedAt(LocalDateTime.now());
            paymentCode.setExpiresAt(expiryDate);
            paymentCode.setVerified(false);
            paymentCode.setParkingTransaction(parkingTransaction);

            paymentVerificationCodeRepository.save(paymentCode);


            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("name", user.getAccountName());
            templateVariables.put("otp", otpCode);
            String emailBody = templateService.processEmailTemplate("payment_otp_verification", templateVariables);

            EmailNotificationRequest emailRequest = new EmailNotificationRequest();
            emailRequest.setTo(user.getEmail());
            emailRequest.setSubject("LegalPark - Kode Verifikasi Pembayaran Anda");
            emailRequest.setBody(emailBody);
            notificationService.sendEmailNotification(emailRequest);

            logger.info("Payment verification code generated and sent to user {}: {}", user.getId(), user.getEmail());
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Payment verification code sent.", null);

        } catch (Exception e) {
            logger.error("Error generating and sending payment verification code for user {}: {}", request.getUserId(), e.getMessage(), e);
//            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate and send payment verification code: " + e.getMessage(), "FAILED");
            throw new RuntimeException("Failed to generate and send payment verification code: " + e.getMessage(), e); // supaya saat error data tidak masuk ke db
        }
    }

    @Override
    public ResponseEntity<Object> validatePaymentVerificationCode(VerifyPaymentCodeRequest request) {
        try {
            Optional<Users> userOptional = usersRepository.findById(request.getUserId());
            if (userOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found.");
            }
            Users user = userOptional.get();


            Optional<PaymentVerificationCode> verificationOptional = paymentVerificationCodeRepository
                    .findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(user, request.getCode());


            if (verificationOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid or expired verification code.");
            }

            PaymentVerificationCode verification = verificationOptional.get();

            if (isCodeExpired(verification.getExpiresAt(), LocalDateTime.now())) {
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Verification code has expired.");
            }


            if (verification.getParkingTransaction() == null || // Jika kode tidak terhubung ke transaksi manapun
                    !verification.getParkingTransaction().getId().equals(request.getParkingTransactionId())) { // Atau ID transaksinya tidak cocok

                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Verification code is not valid for this transaction.");
            }



            verification.setVerified(true);
            paymentVerificationCodeRepository.save(verification);


            logger.info("Payment verification code for user {} validated successfully for transaction {}.", user.getId(), request.getParkingTransactionId());
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Payment verification successful.", null);

        } catch (Exception e) {
            logger.error("Error validating payment verification code for user {}: {}", request.getUserId(), e.getMessage(), e);
//            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to validate payment verification code: " + e.getMessage(), "FAILED");
            throw new RuntimeException("Failed to validate payment verification code: " + e.getMessage(), e);
        }
    
    }
}
