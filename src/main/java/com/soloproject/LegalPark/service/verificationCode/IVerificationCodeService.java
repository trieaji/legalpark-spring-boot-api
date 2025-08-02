package com.soloproject.LegalPark.service.verificationCode;

import com.soloproject.LegalPark.dto.request.verificationCode.PaymentVerificationCodeRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.VerifyPaymentCodeRequest;
import org.springframework.http.ResponseEntity;

public interface IVerificationCodeService {
    /**
     * Menghasilkan dan mengirim kode verifikasi pembayaran ke email pengguna.
     */
    ResponseEntity<Object> generateAndSendPaymentVerificationCode(PaymentVerificationCodeRequest request);

    /**
     * Memvalidasi kode verifikasi pembayaran yang dimasukkan pengguna.
     */
    ResponseEntity<Object> validatePaymentVerificationCode(VerifyPaymentCodeRequest request);


}
