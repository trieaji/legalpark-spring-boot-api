package com.soloproject.LegalPark.service.users;

import com.soloproject.LegalPark.dto.request.users.AccountVerification;
import com.soloproject.LegalPark.entity.AccountStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface IUsersService {

    ResponseEntity<Object> verificationAccount(AccountVerification request);



    /**
     * Memperbarui status akun pengguna (misal: dari PENDING_VERIFICATION ke ACTIVE).
     */
    ResponseEntity<Object> updateAccountStatus(String userId, AccountStatus newStatus);
}
