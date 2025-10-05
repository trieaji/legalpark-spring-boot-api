package com.soloproject.LegalPark.service.balance;

import com.soloproject.LegalPark.dto.request.balance.AddBalanceRequest;
import com.soloproject.LegalPark.dto.request.balance.DeductBalanceRequest;
import org.springframework.http.ResponseEntity;

public interface IBalanceService {

//     Reducing the user's balance.
    ResponseEntity<Object> deductBalance(DeductBalanceRequest request);


//     Add to user balance.
    ResponseEntity<Object> addBalance(AddBalanceRequest request);


//     Retrieve the user's current balance.
    ResponseEntity<Object> getUserBalance(String userId);
}
