package com.soloproject.LegalPark.controller.verificationCode;

import com.soloproject.LegalPark.dto.request.verificationCode.PaymentVerificationCodeRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.VerifyPaymentCodeRequest;
import com.soloproject.LegalPark.service.verificationCode.IVerificationCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/payment/verification")
@Tag(name = "verification code API", description = "Memungkinkan klien (aplikasi mobile/web) untuk secara langsung berinteraksi dengan langkah-langkah yang melibatkan kode verifikasi (meminta dan memverifikasi)")
public class VerificationCodeController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeController.class);

    @Autowired
    private IVerificationCodeService verificationCodeService; // Inject service kita


    @PostMapping("/generate")
    public ResponseEntity<Object> generatePaymentVerificationCode(
            @Validated @RequestBody PaymentVerificationCodeRequest request) {
        logger.info("Received request to generate payment verification code for userId: {}", request.getUserId());
        return verificationCodeService.generateAndSendPaymentVerificationCode(request);
    }


    @PostMapping("/validate")
    public ResponseEntity<Object> validatePaymentVerificationCode(
            @Validated @RequestBody VerifyPaymentCodeRequest request) {
        logger.info("Received request to validate payment verification code for userId: {}", request.getUserId());
        return verificationCodeService.validatePaymentVerificationCode(request);
    }
    
}
