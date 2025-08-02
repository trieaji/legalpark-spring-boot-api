package com.soloproject.LegalPark.service.balance;

import com.soloproject.LegalPark.dto.request.balance.AddBalanceRequest;
import com.soloproject.LegalPark.dto.request.balance.DeductBalanceRequest;
import com.soloproject.LegalPark.dto.response.balance.BalanceResponse;
import com.soloproject.LegalPark.entity.AccountStatus;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class BalanceServiceImpl implements IBalanceService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceServiceImpl.class);

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public ResponseEntity<Object> deductBalance(DeductBalanceRequest request) {
        logger.info("Attempting to deduct balance for user ID: {} with amount: {}", request.getUserId(), request.getAmount());

        try {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Deduct balance failed: Amount to deduct must be positive for user ID: {}", request.getUserId());
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Amount to deduct must be positive.");
            }

            Optional<Users> userOptional = usersRepository.findById(request.getUserId());
            if (userOptional.isEmpty()) {
                logger.warn("Deduct balance failed: User not found with ID: {}", request.getUserId());
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found with ID: " + request.getUserId());
            }
            Users user = userOptional.get();

            // Cek status akun: hanya ACTIVE yang bisa melakukan pembayaran (logika dari UsersServiceImpl)
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                logger.warn("Deduct balance failed: Account is not active for transactions. User ID: {}, Current status: {}", user.getId(), user.getAccountStatus().name());
                return ResponseHandler.generateResponseError(HttpStatus.FORBIDDEN, "FAILED", "Account is not active or verified for transactions. Current status: " + user.getAccountStatus().name());
            }

            if (user.getBalance().compareTo(request.getAmount()) < 0) {
                logger.warn("Deduct balance failed: Insufficient balance for user ID: {}. Current: {}, Attempted deduct: {}", user.getId(), user.getBalance(), request.getAmount());
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Insufficient balance. Current balance: " + user.getBalance());
            }

            BigDecimal newBalance = user.getBalance().subtract(request.getAmount());
            user.setBalance(newBalance);
            usersRepository.save(user);

            logger.info("Balance deducted successfully for user ID: {}. Old: {}, Deducted: {}, New: {}", user.getId(), user.getBalance().add(request.getAmount()), request.getAmount(), newBalance);

            BalanceResponse response = new BalanceResponse(
                    user.getId(),
                    newBalance,
                    "SUCCESS",
                    "Balance deducted successfully.",
                    LocalDateTime.now()
            );
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Balance deducted successfully.", response);
        } catch (Exception e) {
            logger.error("Error deducting balance for user ID {}: {}", request.getUserId(), e.getMessage(), e);
            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to deduct balance: " + e.getMessage(), "FAILED");
        }



    }





    @Override
    public ResponseEntity<Object> addBalance(AddBalanceRequest request) {
        logger.info("Attempting to add balance for user ID: {} with amount: {}", request.getUserId(), request.getAmount());
        try {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Add balance failed: Amount to add must be positive for user ID: {}", request.getUserId());
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Amount to add must be positive.");
            }

            Optional<Users> userOptional = usersRepository.findById(request.getUserId());
            if (userOptional.isEmpty()) {
                logger.warn("Add balance failed: User not found with ID: {}", request.getUserId());
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found with ID: " + request.getUserId());
            }
            Users user = userOptional.get();

            BigDecimal newBalance = user.getBalance().add(request.getAmount());
            user.setBalance(newBalance);
            usersRepository.save(user);

            logger.info("Balance added successfully for user ID: {}. Old: {}, Added: {}, New: {}", user.getId(), user.getBalance().subtract(request.getAmount()), request.getAmount(), newBalance);

            BalanceResponse response = new BalanceResponse(
                    user.getId(),
                    newBalance,
                    "SUCCESS",
                    "Balance added successfully.",
                    LocalDateTime.now()
            );
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Balance added successfully.", response);
        }  catch (Exception e) {
            logger.error("Error adding balance for user ID {}: {}", request.getUserId(), e.getMessage(), e);
            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add balance: " + e.getMessage(), "FAILED");
        }
    }

    @Override
    public ResponseEntity<Object> getUserBalance(String userId) {
        try {
            Optional<Users> userOptional = usersRepository.findById(userId);
            if (userOptional.isEmpty()) {
                logger.warn("Get user balance failed: User not found with ID {}", userId);
                return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found.");
            }

            Users user = userOptional.get();
            BigDecimal currentBalance = user.getBalance();

            BalanceResponse response = new BalanceResponse(
                    user.getId(),
                    currentBalance,
                    "SUCCESS",
                    "User balance retrieved successfully.",
                    LocalDateTime.now()
            );
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "User balance retrieved successfully.", response);

        } catch (Exception e) {
            logger.error("Error retrieving balance for user {}: {}", userId, e.getMessage(), e);
            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve balance: " + e.getMessage(), "FAILED");
        }
    }
    
}
