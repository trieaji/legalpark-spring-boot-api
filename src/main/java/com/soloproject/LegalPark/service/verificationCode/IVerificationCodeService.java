package com.soloproject.LegalPark.service.verificationCode;

import com.soloproject.LegalPark.dto.request.verificationCode.PaymentVerificationCodeRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.VerifyPaymentCodeRequest;
import org.springframework.http.ResponseEntity;

public interface IVerificationCodeService {
    /**
     * Generate and send payment verification codes to users' email addresses.
     */
    ResponseEntity<Object> generateAndSendPaymentVerificationCode(PaymentVerificationCodeRequest request);

    /**
     * Validate the payment verification code entered by the user.
     */
    ResponseEntity<Object> validatePaymentVerificationCode(VerifyPaymentCodeRequest request);


}
