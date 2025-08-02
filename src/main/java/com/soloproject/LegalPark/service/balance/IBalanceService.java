package com.soloproject.LegalPark.service.balance;

import com.soloproject.LegalPark.dto.request.balance.AddBalanceRequest;
import com.soloproject.LegalPark.dto.request.balance.DeductBalanceRequest;
import org.springframework.http.ResponseEntity;

public interface IBalanceService {

//     Mengurangi saldo pengguna.
    ResponseEntity<Object> deductBalance(DeductBalanceRequest request);


//     Menambah saldo pengguna.
    ResponseEntity<Object> addBalance(AddBalanceRequest request);


//     Mendapatkan saldo pengguna saat ini.
    ResponseEntity<Object> getUserBalance(String userId);
}
