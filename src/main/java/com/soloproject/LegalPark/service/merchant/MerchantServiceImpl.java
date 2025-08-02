package com.soloproject.LegalPark.service.merchant;

import com.soloproject.LegalPark.dto.request.merchant.MerchantRequest;
import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.CodeGeneratorUtil;
import com.soloproject.LegalPark.repository.MerchantRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class MerchantServiceImpl implements IMerchantService{

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    private CodeGeneratorUtil codeGeneratorUtil;

    
    @Override
    public ResponseEntity<Object> createNewMerchant(MerchantRequest request) {
        
        ModelMapper modelMapper = new ModelMapper();
        Merchant merchantMapper = modelMapper.map(request, Merchant.class);
        String uniqueShortCode = codeGeneratorUtil.generateUniqueMerchantShortCode();
        merchantMapper.setMerchantCode(uniqueShortCode);
        merchantMapper.setMerchantName(request.getMerchantName());
        merchantMapper.setMerchantAddress(request.getMerchantAddress());
        merchantMapper.setContactPerson(request.getContactPerson());
        merchantMapper.setContactPhone(request.getContactPhone());

        var data = merchantRepository.save(merchantMapper);

        MerchantResponse response = new MerchantResponse();
        response.setId(data.getId());
        response.setMerchantCode(data.getMerchantCode());
        response.setMerchantName(data.getMerchantName());
        response.setMerchantAddress(data.getMerchantAddress());
        response.setContactPerson(data.getContactPerson());
        response.setContactPhone(data.getContactPhone());

        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> getAllMerchants() {
        var data = merchantRepository.findAll();
        return ResponseHandler.generateResponseSuccess(data);
    }


    @Override
    public ResponseEntity<Object> deleteMerchant(String id) {
        var data = merchantRepository.findById(id).orElseThrow();
        merchantRepository.deleteById(data.getId());
        return ResponseHandler.generateResponseSuccess(data);
    }

    @Override
    public ResponseEntity<Object> updateExistingMerchant(String id, MerchantRequest request) {
        // 1. Cari merchant yang ada berdasarkan ID
        Optional<Merchant> existingMerchantOptional = merchantRepository.findById(id);

        // 2. Jika merchant tidak ditemukan, kembalikan error NOT_FOUND
        if (existingMerchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant with ID " + id + " not found.");
        }

        // 3. Ambil objek Merchant yang sudah ada dari Optional
        Merchant existingMerchant = existingMerchantOptional.get();

        // 4. Perbarui hanya properti yang disediakan (tidak null) dalam request
        if (request.getMerchantName() != null) {
            existingMerchant.setMerchantName(request.getMerchantName());
        }
        if (request.getMerchantAddress() != null) {
            existingMerchant.setMerchantAddress(request.getMerchantAddress());
        }
        if (request.getContactPerson() != null) {
            existingMerchant.setContactPerson(request.getContactPerson());
        }
        if (request.getContactPhone() != null) {
            existingMerchant.setContactPhone(request.getContactPhone());
        }

        // 5. Simpan (merge) perubahan ke database
        Merchant updatedMerchant = merchantRepository.save(existingMerchant);
        
        return ResponseHandler.generateResponseSuccess(updatedMerchant);
    }

    @Override
    public ResponseEntity<Object> getMerchantByCode(MerchantRequest request) {
        var data = merchantRepository.findByMerchantCode(request.getMerchantCode());

        if (data.isPresent()) {
            Merchant merchant = data.get(); //Ambil datanya jika sudah ditemukan atau datanya ada
            
            MerchantResponse response = new MerchantResponse();
            response.setId(merchant.getId());
            response.setMerchantCode(merchant.getMerchantCode());
            response.setMerchantName(merchant.getMerchantName());
            response.setMerchantAddress(merchant.getMerchantAddress());
            response.setContactPerson(merchant.getContactPerson());
            response.setContactPhone(merchant.getContactPhone());

            return ResponseHandler.generateResponseSuccess(response);
        } else {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + request.getMerchantCode());
        }
    }
}
