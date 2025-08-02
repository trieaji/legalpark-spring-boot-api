package com.soloproject.LegalPark.service.report.admin;

import com.soloproject.LegalPark.dto.response.report.AdminDailyRevenueReportResponse;
import com.soloproject.LegalPark.dto.response.report.AdminParkingSpotOccupancyReportResponse;
import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.entity.ParkingSpot;
import com.soloproject.LegalPark.entity.ParkingSpotStatus;
import com.soloproject.LegalPark.entity.ParkingTransaction;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.ParkingSpotRepository;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminReportServiceImpl implements IAdminReportService{

    @Autowired
    private ParkingTransactionRepository parkingTransactionRepository;
    @Autowired
    private ParkingSpotRepository parkingSpotRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ResponseEntity<Object> getDailyRevenueReport(LocalDate date, String merchantCode) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<ParkingTransaction> paidTransactions;

        if (merchantCode != null && !merchantCode.isEmpty()) {
            Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(merchantCode);
            if (merchantOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + merchantCode);
            }

            paidTransactions = parkingTransactionRepository.findPaidTransactionsByExitTimeBetweenAndMerchantCode(startOfDay, endOfDay, merchantCode);
        } else {

            paidTransactions = parkingTransactionRepository.findPaidTransactionsByExitTimeBetween(startOfDay, endOfDay);
        }

        if (paidTransactions.isEmpty()) {
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "No revenue recorded for " + date + (merchantCode != null ? " at merchant " + merchantCode : "") + ".", List.of());
        }

        // Mengelompokkan berdasarkan merchant jika tidak ada kode spesifik merchant yang diminta
        if (merchantCode == null || merchantCode.isEmpty()) {
            Map<String, List<ParkingTransaction>> transactionsByMerchant = paidTransactions.stream()
                    .filter(t -> t.getParkingSpot() != null && t.getParkingSpot().getMerchant() != null)
                    .collect(Collectors.groupingBy(t -> t.getParkingSpot().getMerchant().getMerchantCode()));

            List<AdminDailyRevenueReportResponse> responses = new ArrayList<>();
            transactionsByMerchant.forEach((mCode, transactions) -> {
                BigDecimal totalRevenue = transactions.stream()
                        .filter(t -> t.getTotalCost() != null)
                        .map(ParkingTransaction::getTotalCost)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                responses.add(createDailyRevenueResponse(date, mCode, transactions.get(0).getParkingSpot().getMerchant().getMerchantName(), totalRevenue, transactions.size()));
            });
//            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Daily revenue report retrieved successfully for all merchants.", responses);
            return ResponseHandler.generateResponseSuccess(responses);
        } else {
            // Untuk merchant tertentu, hitung total pendapatan secara langsung
            BigDecimal totalRevenue = paidTransactions.stream()
                    .filter(t -> t.getTotalCost() != null)
                    .map(ParkingTransaction::getTotalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String mName = paidTransactions.get(0).getParkingSpot().getMerchant().getMerchantName();
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Daily revenue report retrieved successfully for merchant " + mName + ".",
                   List.of(createDailyRevenueResponse(date, merchantCode, mName, totalRevenue, paidTransactions.size())));

        }
    }

    private AdminDailyRevenueReportResponse createDailyRevenueResponse(LocalDate date, String merchantCode, String merchantName, BigDecimal totalRevenue, long totalPaidTransactions) {
        AdminDailyRevenueReportResponse response = new AdminDailyRevenueReportResponse();
        response.setDate(date);
        response.setMerchantCode(merchantCode);
        response.setMerchantName(merchantName);
        response.setTotalRevenue(totalRevenue);
        response.setTotalPaidTransactions(totalPaidTransactions);
        return response;
    }


    @Override
    public ResponseEntity<Object> getParkingSpotOccupancyReport(String merchantCode, String status) {
        List<ParkingSpot> parkingSpots;

        if (merchantCode != null && !merchantCode.isEmpty()) {
            Optional<Merchant> merchantOptional = merchantRepository.findByMerchantCode(merchantCode);
            if (merchantOptional.isEmpty()) {
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Merchant not found with code: " + merchantCode);
            }
            if (status != null && !status.isEmpty()) {
                try {
                    ParkingSpotStatus spotStatus = ParkingSpotStatus.valueOf(status.toUpperCase());
                    parkingSpots = parkingSpotRepository.findByMerchantAndStatus(merchantOptional.get(), spotStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid parking spot status: " + status);
                }
            } else {
                parkingSpots = parkingSpotRepository.findByMerchant(merchantOptional.get());
            }
        } else {
            if (status != null && !status.isEmpty()) {
                try {
                    ParkingSpotStatus spotStatus = ParkingSpotStatus.valueOf(status.toUpperCase());
                    parkingSpots = parkingSpotRepository.findByStatus(spotStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid parking spot status: " + status);
                }
            } else {
                parkingSpots = parkingSpotRepository.findAll();
            }
        }

        if (parkingSpots.isEmpty()) {
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "No parking spots found matching criteria.", List.of());
        }

        List<AdminParkingSpotOccupancyReportResponse> responses = new ArrayList<>();
        for (ParkingSpot spot : parkingSpots) {
            AdminParkingSpotOccupancyReportResponse response = new AdminParkingSpotOccupancyReportResponse();
            response.setSpotId(spot.getId());
            response.setSpotNumber(spot.getSpotNumber());
            response.setSpotType(spot.getSpotType().name());
            response.setCurrentStatus(spot.getStatus().name());
            if (spot.getMerchant() != null) {
                response.setMerchantCode(spot.getMerchant().getMerchantCode());
                response.setMerchantName(spot.getMerchant().getMerchantName());
            }

            // Jika slot ditempati, maka cari transaksi aktif dan kendaraan yang menempati
            if (spot.getStatus() == ParkingSpotStatus.OCCUPIED) {
                Optional<ParkingTransaction> activeTransactionOptional = parkingTransactionRepository.findByParkingSpotAndStatus(spot, com.soloproject.LegalPark.entity.ParkingStatus.ACTIVE);
                if (activeTransactionOptional.isPresent()) {
                    ParkingTransaction activeTransaction = activeTransactionOptional.get();
                    if (activeTransaction.getVehicle() != null) {
                        response.setCurrentVehicleLicensePlate(activeTransaction.getVehicle().getLicensePlate());
                        response.setCurrentVehicleType(activeTransaction.getVehicle().getType().name());
                        if (activeTransaction.getVehicle().getOwner() != null) {
                            response.setCurrentOccupantUserName(activeTransaction.getVehicle().getOwner().getAccountName());
                        }
                    }
                }
            }
            responses.add(response);
        }

        return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Parking spot occupancy report retrieved successfully.", responses);
    }
    
}
