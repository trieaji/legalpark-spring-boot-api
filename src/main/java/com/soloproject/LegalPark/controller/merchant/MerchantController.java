package com.soloproject.LegalPark.controller.merchant;

import com.soloproject.LegalPark.dto.request.merchant.MerchantRequest;
import com.soloproject.LegalPark.service.merchant.IMerchantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Merchant Controller", description = "Merchant Service")
public class MerchantController {

    @Autowired
    IMerchantService iMerchantService;

    @PostMapping("/merchants")
    public ResponseEntity<Object> createMerchant(@RequestBody MerchantRequest request){
        return iMerchantService.createNewMerchant(request);
    }

    @GetMapping("/merchants/find")
    public ResponseEntity<Object> getAllMerchants(){
        return iMerchantService.getAllMerchants();
    }

    @PatchMapping("/merchants/update/{id}")
    public ResponseEntity<Object> updateMerchant(@PathVariable("id") String id, @RequestBody MerchantRequest request){
        return iMerchantService.updateExistingMerchant(id, request);
    }

    @DeleteMapping("/merchants/delete/{id}")
    public ResponseEntity<Object> deleteMerchant(@PathVariable("id") String id){
        return iMerchantService.deleteMerchant(id);
    }

    @GetMapping("/merchants/get-by-code")
    public ResponseEntity<Object> getMerchantByCode(@RequestBody MerchantRequest request) {
        return iMerchantService.getMerchantByCode(request);
    }
}
