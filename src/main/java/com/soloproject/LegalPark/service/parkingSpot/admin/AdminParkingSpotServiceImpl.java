package com.soloproject.LegalPark.service.parkingSpot.admin;

import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotRequest;
import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotUpdateRequest;
import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.entity.ParkingSpot;
import com.soloproject.LegalPark.entity.ParkingSpotStatus;
import com.soloproject.LegalPark.entity.SpotType;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ParkingSpotResponseMapper;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.ParkingSpotRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminParkingSpotServiceImpl implements IAdminParkingSpotService{

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    ParkingSpotResponseMapper parkingSpotResponseMapper;


    @Override
    public ResponseEntity<Object> adminCreateParkingSpot(ParkingSpotRequest request) {
        // 1. Cari Merchant berdasarkan merchantCode
        Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(request.getMerchantCode());
        if (merchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + request.getMerchantCode());
        }
        Merchant merchant = merchantOptional.get();

        // 2. Cek apakah spotNumber sudah ada di merchant yang sama (sesuai uniqueConstraint)
        Optional<ParkingSpot> existingSpot = parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant);
        if (existingSpot.isPresent()) {
            return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Parking spot with number '" + request.getSpotNumber() + "' already exists for this merchant.");
        }

        // 3. Konversi DTO ke Entity
        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setSpotNumber(request.getSpotNumber());
        parkingSpot.setFloor(request.getFloor());
        parkingSpot.setMerchant(merchant);

        // Konversi String ke Enum SpotType
        try {
            parkingSpot.setSpotType(SpotType.valueOf(request.getSpotType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid spot type: " + request.getSpotType());
        }

        // Set status awal sebagai AVAILABLE
        parkingSpot.setStatus(ParkingSpotStatus.AVAILABLE);

        // 4. Simpan ke database
        ParkingSpot savedParkingSpot = parkingSpotRepository.save(parkingSpot);

        // 5. Konversi Entity yang disimpan ke DTO Response
        ParkingSpotResponse response = parkingSpotResponseMapper.mapToParkingSpotResponse(savedParkingSpot);
        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> adminGetAllParkingSpots() {
        List<ParkingSpot> parkingSpots = parkingSpotRepository.findAll();
        List<ParkingSpotResponse> responses = parkingSpots.stream()
                .map(parkingSpotResponseMapper::mapToParkingSpotResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingSpotById(String id) {
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotRepository.findById(id);
        if (parkingSpotOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking spot not found with ID: " + id);
        }
        ParkingSpot parkingSpot = parkingSpotOptional.get();
        ParkingSpotResponse response = parkingSpotResponseMapper.mapToParkingSpotResponse(parkingSpot);
        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> adminUpdateParkingSpot(String id, ParkingSpotUpdateRequest request) {
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotRepository.findById(id);
        if (parkingSpotOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking spot not found with ID: " + id);
        }
        ParkingSpot parkingSpot = parkingSpotOptional.get();

        // Perbarui field yang tidak null di request
        if (request.getSpotNumber() != null) {
            // Jika spotNumber diubah, cek keunikan dengan merchant saat ini
            Optional<ParkingSpot> existingSpot = parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), parkingSpot.getMerchant());
            if (existingSpot.isPresent() && !existingSpot.get().getId().equals(id)) { // Pastikan bukan dirinya sendiri
                return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Parking spot with number '" + request.getSpotNumber() + "' already exists for this merchant.");
            }
            parkingSpot.setSpotNumber(request.getSpotNumber());
        }

        if (request.getSpotType() != null) {
            try {
                parkingSpot.setSpotType(SpotType.valueOf(request.getSpotType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid spot type: " + request.getSpotType());
            }
        }

        if (request.getStatus() != null) {
            try {
                parkingSpot.setStatus(ParkingSpotStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid status: " + request.getStatus());
            }
        }

        if (request.getFloor() != null) {
            parkingSpot.setFloor(request.getFloor());
        }

        // Jika merchantCode di request tidak null dan berbeda dari merchant saat ini
        if (request.getMerchantCode() != null && !request.getMerchantCode().equals(parkingSpot.getMerchant().getMerchantCode())) {
            Optional<Merchant> newMerchantOptional = merchantRepository.findByMerchantCode(request.getMerchantCode());
            if (newMerchantOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "New Merchant not found with code: " + request.getMerchantCode());
            }
            Merchant newMerchant = newMerchantOptional.get();

            // Cek keunikan spotNumber di merchant yang baru (jika spotNumber juga diubah)
            if (request.getSpotNumber() != null) {
                Optional<ParkingSpot> existingSpotInNewMerchant = parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), newMerchant);
                if (existingSpotInNewMerchant.isPresent()) {
                    return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Parking spot with number '" + request.getSpotNumber() + "' already exists for the new merchant.");
                }
            } else { // Jika spotNumber tidak diubah, pakai spotNumber yang lama untuk cek keunikan di merchant baru
                Optional<ParkingSpot> existingSpotInNewMerchant = parkingSpotRepository.findBySpotNumberAndMerchant(parkingSpot.getSpotNumber(), newMerchant);
                if (existingSpotInNewMerchant.isPresent()) {
                    return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Parking spot with number '" + parkingSpot.getSpotNumber() + "' already exists for the new merchant.");
                }
            }

            parkingSpot.setMerchant(newMerchant);
        }

        // Simpan perubahan
        ParkingSpot updatedParkingSpot = parkingSpotRepository.save(parkingSpot);

        ParkingSpotResponse response = parkingSpotResponseMapper.mapToParkingSpotResponse(updatedParkingSpot);
        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> adminDeleteParkingSpot(String id) {
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotRepository.findById(id);
        if (parkingSpotOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Parking spot not found with ID: " + id);
        }
        parkingSpotRepository.deleteById(id);
        return ResponseHandler.generateResponseSuccess(null);
    }

    @Override
    public ResponseEntity<Object> adminGetParkingSpotsByMerchant(String merchantIdentifier) {
        Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(merchantIdentifier);

        if (merchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with identifier: " + merchantIdentifier);
        }
        Merchant merchant = merchantOptional.get();

        List<ParkingSpot> parkingSpots = parkingSpotRepository.findByMerchant(merchant);
        List<ParkingSpotResponse> responses = parkingSpots.stream()
                .map(parkingSpotResponseMapper::mapToParkingSpotResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }
}
