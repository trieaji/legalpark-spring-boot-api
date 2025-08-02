package com.soloproject.LegalPark.service.users;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.users.AccountVerification;
import com.soloproject.LegalPark.entity.AccountStatus;
import com.soloproject.LegalPark.entity.LogVerification;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.GenerateOtp;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.helper.MailService;
import com.soloproject.LegalPark.repository.LogVerificationRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class UsersServiceImpl implements IUsersService {

    @Autowired
    InfoAccount infoAccount;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    LogVerificationRepository logVerificationRepository;

    @Autowired
    MailService mailService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private ITemplateService templateService;

    @Override
    public ResponseEntity<Object> verificationAccount(AccountVerification request) {
        try{
            String user = infoAccount.get().getEmail(); //Untuk menemukan email

            LogVerification verify = logVerificationRepository.getByUserAndExp(user,request.getCode()); //mendapatkan email dan code

            if(verify == null){ //jika email dan code kosong maka, kembalikan error
                return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST,"FAILED","Otp yang anda masukan salah");
            }

            if(verify.getExpired() != null) {
                LocalDateTime getExpi = verify.getExpired();
                LocalDateTime currentTime = LocalDateTime.now();

                if(isExpired(getExpi, currentTime)) {
                    LogVerification verification = new LogVerification();
                    verification.setCode(GenerateOtp.generateRandomNumber());
                    verification.setExpired(GenerateOtp.getExpiryDate());
                    var saveLog = logVerificationRepository.save(verification);
                    return ResponseHandler.generateResponseSuccess(saveLog);

                } else {
                    System.out.println("Masih berlaku dek");
                }
            }

            verify.setVerify(true);
            logVerificationRepository.save(verify);

            Users data = usersRepository.findByEmail(user).orElseThrow();
            data.setAccountStatus(AccountStatus.ACTIVE);
            var save = usersRepository.save(data);

//            Context context = new Context();
//
//            context.setVariable("name", save.getAccountName());
//            String emailContent = templateEngine.process("email_success_verification", context);
//            mailService.sendEmail(data.getEmail(),"Success Verification", emailContent);

            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("name", save.getAccountName());
            String emailBody = templateService.processEmailTemplate("email_success_verification", templateVariables); // Pastikan nama template "email_success_verification" sesuai dengan nama file Anda

            // Gantikan mailService.sendEmail() dengan NotificationService
            EmailNotificationRequest emailRequest = new EmailNotificationRequest();
            emailRequest.setTo(data.getEmail());
            emailRequest.setSubject("Success Verification");
            emailRequest.setBody(emailBody);
            notificationService.sendEmailNotification(emailRequest);
            
            return ResponseHandler.generateResponseSuccess(save);

        }catch (Exception e){
            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),"");
        }
    }


    @Override
    public ResponseEntity<Object> updateAccountStatus(String userId, AccountStatus newStatus) {
        Optional<Users> userOptional = usersRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found with ID: " + userId);
        }
        Users user = userOptional.get();

        // Validasi transisi status
        if (user.getAccountStatus() == AccountStatus.BLOCKED && newStatus != AccountStatus.ACTIVE) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Cannot change status from BLOCKED directly, unless unblocked by admin.");
        }

        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION &&
                !(newStatus == AccountStatus.ACTIVE || newStatus == AccountStatus.BLOCKED)) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Invalid status transition from PENDING_VERIFICATION.");
        }

        user.setAccountStatus(newStatus);
        usersRepository.save(user);
        return ResponseHandler.generateResponseSuccess(user);

    }

    public static boolean isExpired(LocalDateTime expiredTime, LocalDateTime currentTime) {
        return Duration.between(expiredTime,currentTime).toMinutes() >= 5;
    }
}
