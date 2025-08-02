package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.PaymentVerificationCode;
import com.soloproject.LegalPark.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentVerificationCodeRepository extends JpaRepository<PaymentVerificationCode, String> {
    // Mencari kode verifikasi yang belum diverifikasi untuk user dan kode tertentu, diurutkan berdasarkan waktu kadaluarsa terbaru
    Optional<PaymentVerificationCode> findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(Users user, String code);

    // Mencari kode verifikasi aktif untuk user tertentu (jika hanya ada satu yang aktif pada satu waktu)
    Optional<PaymentVerificationCode> findTopByUserAndIsVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(Users user, LocalDateTime currentTime);
}
