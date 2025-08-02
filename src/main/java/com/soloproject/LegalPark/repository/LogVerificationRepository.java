package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.LogVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogVerificationRepository extends JpaRepository<LogVerification,String> {

    @Query("SELECT l FROM LogVerification l WHERE l.user.email = :email AND l.code = :otp AND l.isVerify = false")
    LogVerification getByUserAndExp(@Param("email") String email, @Param("otp") String otp);

    LogVerification findByUserEmail(String email);

}
