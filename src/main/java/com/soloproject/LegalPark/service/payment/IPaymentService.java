package com.soloproject.LegalPark.service.payment;

import com.soloproject.LegalPark.util.PaymentResult;

import java.math.BigDecimal;

public interface IPaymentService {
    /**
     * Memproses pembayaran untuk transaksi parkir tertentu.
     */
    PaymentResult processParkingPayment(String userId, BigDecimal amount, String parkingTransactionId, String verificationCode);

}
