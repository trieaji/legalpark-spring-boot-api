package com.soloproject.LegalPark.service.report.users;

import com.soloproject.LegalPark.dto.response.report.UserParkingHistoryReportResponse;
import com.soloproject.LegalPark.dto.response.report.UserSummaryReportResponse;
import com.soloproject.LegalPark.entity.ParkingTransaction;
import com.soloproject.LegalPark.entity.PaymentStatus;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ReportResponseMapper;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserReportServiceImpl implements IUserReportService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ParkingTransactionRepository parkingTransactionRepository;

    @Autowired
    ReportResponseMapper reportResponseMapper;


    @Override
    public ResponseEntity<Object> getUserParkingHistory(String userId, LocalDate startDate, LocalDate endDate) {
        Optional<Users> userOptional = usersRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found with ID: " + userId);
        }
        Users user = userOptional.get();


        // Convert LocalDate ke LocalDateTime untuk repository query (awal hari, akhir hari)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // Akhir hari


        List<ParkingTransaction> transactions = parkingTransactionRepository
                .findByVehicleOwnerIdAndEntryTimeBetween(userId, startDateTime, endDateTime);

        if (transactions.isEmpty()) {
//            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "No parking history found for this user in the specified period.", List.of());
            return ResponseHandler.generateResponseSuccess(List.of());
        }

        List<UserParkingHistoryReportResponse> responses = transactions.stream()
                .map(reportResponseMapper::mapToUserParkingHistoryReportResponse)
                .collect(Collectors.toList());

//        return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "User parking history retrieved successfully.", responses);
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> getUserSummaryReport(String userId) {
        Optional<Users> userOptional = usersRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found with ID: " + userId);
        }
        Users user = userOptional.get();

        UserSummaryReportResponse summary = new UserSummaryReportResponse();
        summary.setUserId(user.getId());
        summary.setUserName(user.getAccountName());
        summary.setCurrentBalance(user.getBalance());

        // Hitung total sesi parkir dan total biaya
        List<ParkingTransaction> userTransactions = parkingTransactionRepository
                .findByVehicleOwnerId(userId);

        long totalSessions = userTransactions.size();
        BigDecimal totalCost = userTransactions.stream()
                .filter(t -> t.getPaymentStatus() == PaymentStatus.PAID && t.getTotalCost() != null)
                .map(ParkingTransaction::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.setTotalParkingSessions(totalSessions);
        summary.setTotalCostSpent(totalCost);

//        return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "User summary report retrieved successfully.", summary);
        return ResponseHandler.generateResponseSuccess(summary);
    }
    
}
