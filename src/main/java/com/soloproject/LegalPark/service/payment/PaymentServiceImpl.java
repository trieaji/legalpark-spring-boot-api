package com.soloproject.LegalPark.service.payment;

import com.soloproject.LegalPark.dto.request.balance.DeductBalanceRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.VerifyPaymentCodeRequest;
import com.soloproject.LegalPark.entity.ParkingTransaction;
import com.soloproject.LegalPark.entity.PaymentStatus;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.service.balance.IBalanceService;
import com.soloproject.LegalPark.service.users.IUsersService;
import com.soloproject.LegalPark.service.verificationCode.IVerificationCodeService;
import com.soloproject.LegalPark.util.PaymentResult;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements IPaymentService{

    @Autowired
    private IUsersService usersService;

    @Autowired
    private IVerificationCodeService verificationCodeService;

    @Autowired
    private IBalanceService balanceService;

    @Autowired
    private ParkingTransactionRepository parkingTransactionRepository;

    /**
     * Memproses pembayaran simulasi untuk transaksi parkir.
     */
    @Override
    public PaymentResult processParkingPayment(String userId, BigDecimal amount, String parkingTransactionId, String verificationCode) {
        // Logika simulasi pembayaran
        System.out.println("Processing payment for User ID: " + userId +
                ", Amount: " + amount +
                ", Parking Transaction ID: " + parkingTransactionId);

        // =========================================================
        // 1. Validasi Kode Verifikasi Pembayaran
        // =========================================================
        VerifyPaymentCodeRequest verifyCodeRequest = new VerifyPaymentCodeRequest();
        verifyCodeRequest.setUserId(userId); // Gunakan userId dari parameter
        verifyCodeRequest.setCode(verificationCode);
        verifyCodeRequest.setParkingTransactionId(parkingTransactionId);

        // Panggilan ke VerificationCodeService untuk validasi
        ResponseEntity<Object> verificationResponse = verificationCodeService.validatePaymentVerificationCode(verifyCodeRequest);

        if (verificationResponse.getStatusCode() != HttpStatus.OK) {
            System.out.println("Payment FAILED: Verification code invalid or expired for User ID: " + userId);

            // Contoh: Mengambil pesan error dari verificationResponse
            if (verificationResponse.getBody() instanceof Map) {
                Map<String, Object> errorBody = (Map<String, Object>) verificationResponse.getBody();
                if (errorBody.containsKey("message")) {
                    System.out.println("Verification Error Message: " + errorBody.get("message"));
                }
            }
            return PaymentResult.FAILED_OTHER; // Atau buat enum PaymentResult.INVALID_VERIFICATION_CODE
        }
        System.out.println("Verification code validated successfully for User ID: " + userId);



        // 2. Memanggil BalanceService untuk mengurangi saldo
        // Buat objek permintaan untuk BalanceService
        DeductBalanceRequest deductRequest = new DeductBalanceRequest(userId, amount);

        // Memanggil UsersService untuk mengurangi saldo
//        ResponseEntity<Object> deductResponse = usersService.deduct(userId, amount);
        ResponseEntity<Object> deductResponse = balanceService.deductBalance(deductRequest);

        if (deductResponse.getStatusCode() == HttpStatus.OK) {
            // 3. Update Status Transaksi Parkir (INI PENTING)
            // Contoh implementasi di sini:
            if (parkingTransactionRepository != null) { // Pastikan repository tidak null jika disuntikkan
                Optional<ParkingTransaction> ptOpt = parkingTransactionRepository.findById(parkingTransactionId);
                if (ptOpt.isPresent()) {
                    ParkingTransaction pt = ptOpt.get();
                    pt.setPaymentStatus(PaymentStatus.PAID);
                    parkingTransactionRepository.save(pt);
                    System.out.println("Parking Transaction " + parkingTransactionId + " updated to PAID.");
                } else {
                    System.err.println("Warning: Parking Transaction " + parkingTransactionId + " not found after payment. This might indicate a data inconsistency.");
                    return PaymentResult.FAILED_OTHER; // Mengembalikan gagal karena update transaksi parkir bermasalah
                }
            } else {
                System.err.println("Warning: ParkingTransactionRepository not injected. Cannot update parking transaction status.");
                return PaymentResult.FAILED_OTHER; // Mengembalikan gagal karena repository tidak tersedia
            }
            
            System.out.println("Payment SUCCESS for User ID: " + userId);
            return PaymentResult.SUCCESS;
        } else if (deductResponse.getStatusCode() == HttpStatus.BAD_REQUEST &&
                deductResponse.getBody() instanceof Map &&
                ((Map<String, Object>) deductResponse.getBody()).containsKey("message") &&
                ((String) ((Map<String, Object>) deductResponse.getBody()).get("message")).contains("Insufficient balance")) {
            System.out.println("Payment FAILED: Insufficient Balance for User ID: " + userId);
            return PaymentResult.INSUFFICIENT_BALANCE;
        } else {
            System.out.println("Payment FAILED: Other reason for User ID: " + userId +
                    ". Status: " + deductResponse.getStatusCode() +
                    ", Message: " + deductResponse.getBody());
            return PaymentResult.FAILED_OTHER;
        }
    }
}
