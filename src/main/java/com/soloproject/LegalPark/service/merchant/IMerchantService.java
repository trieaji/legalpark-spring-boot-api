package com.soloproject.LegalPark.service.merchant;

import com.soloproject.LegalPark.dto.request.merchant.MerchantRequest;
import org.springframework.http.ResponseEntity;

public interface IMerchantService {
    ResponseEntity<Object> createNewMerchant(MerchantRequest request);
    ResponseEntity<Object> getAllMerchants();
    ResponseEntity<Object> deleteMerchant(String id);
    ResponseEntity<Object> updateExistingMerchant(String id, MerchantRequest request);
    ResponseEntity<Object> getMerchantByCode (MerchantRequest request);
}
