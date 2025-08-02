package com.soloproject.LegalPark.service.parkingSpot.users;

import com.soloproject.LegalPark.dto.request.parkingSpot.AvailableSpotFilterRequest;
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
public class UserParkingSpotServiceImpl implements IUserParkingSpotService {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    ParkingSpotResponseMapper parkingSpotResponseMapper;


    @Override
    public ResponseEntity<Object> userGetAvailableParkingSpots(AvailableSpotFilterRequest filter) {
        List<ParkingSpot> availableSpots;

        // Logika filter:
        if (filter.getMerchantCode() != null && !filter.getMerchantCode().isEmpty()) {
            Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(filter.getMerchantCode());
            if (merchantOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + filter.getMerchantCode());
            }
            Merchant merchant = merchantOptional.get();

            SpotType spotTypeFilter = null;
            if (filter.getSpotType() != null && !filter.getSpotType().isEmpty()) {
                try {
                    spotTypeFilter = SpotType.valueOf(filter.getSpotType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid spot type: " + filter.getSpotType());
                }
            }

            if (spotTypeFilter != null) {
                // Cari berdasarkan merchant, status AVAILABLE, dan spot type
                availableSpots = parkingSpotRepository.findByMerchantAndStatusAndSpotType(merchant, ParkingSpotStatus.AVAILABLE, spotTypeFilter);
            } else {
                // Cari berdasarkan merchant dan status AVAILABLE
                availableSpots = parkingSpotRepository.findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE);
            }

            // Tambahkan filter floor jika ada
            if (filter.getFloor() != null) {
                availableSpots = availableSpots.stream()
                        .filter(spot -> spot.getFloor() != null && spot.getFloor().equals(filter.getFloor()))
                        .collect(Collectors.toList());
            }

        } else {
            // Jika tidak ada merchantCode, cari semua yang AVAILABLE di seluruh sistem
            availableSpots = parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE);

            // Tambahkan filter floor jika ada (hanya jika tidak difilter berdasarkan merchant)
            if (filter.getFloor() != null) {
                availableSpots = availableSpots.stream()
                        .filter(spot -> spot.getFloor() != null && spot.getFloor().equals(filter.getFloor()))
                        .collect(Collectors.toList());
            }

            // Tambahkan filter spotType jika ada (hanya jika tidak difilter berdasarkan merchant)
            if (filter.getSpotType() != null && !filter.getSpotType().isEmpty()) {
                SpotType spotTypeFilter = null;
                try {
                    spotTypeFilter = SpotType.valueOf(filter.getSpotType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid spot type: " + filter.getSpotType());
                }
                SpotType finalSpotTypeFilter = spotTypeFilter;
                availableSpots = availableSpots.stream()
                        .filter(spot -> spot.getSpotType().equals(finalSpotTypeFilter))
                        .collect(Collectors.toList());
            }
        }

        List<ParkingSpotResponse> responses = availableSpots.stream()
                .map(parkingSpotResponseMapper::mapToParkingSpotResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> userGetParkingSpotsByMerchant(String merchantCode) {
        Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(merchantCode);
        if (merchantOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + merchantCode);
        }
        Merchant merchant = merchantOptional.get();

        List<ParkingSpot> parkingSpots = parkingSpotRepository.findByMerchant(merchant);
        List<ParkingSpotResponse> responses = parkingSpots.stream()
                .map(parkingSpotResponseMapper::mapToParkingSpotResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }
}

