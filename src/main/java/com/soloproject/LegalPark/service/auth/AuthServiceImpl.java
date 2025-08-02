package com.soloproject.LegalPark.service.auth;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.users.LoginRequest;
import com.soloproject.LegalPark.dto.request.users.RegisterRequest;
import com.soloproject.LegalPark.dto.response.users.RegisterResponse;
import com.soloproject.LegalPark.dto.response.users.SignResponse;
import com.soloproject.LegalPark.entity.AccountStatus;
import com.soloproject.LegalPark.entity.LogVerification;
import com.soloproject.LegalPark.entity.Role;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.GenerateOtp;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.helper.MailService;
import com.soloproject.LegalPark.repository.LogVerificationRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.security.jwt.JwtService;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements IAuthService {
    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtService jwtService;

    @Autowired
    InfoAccount infoAccount;

    @Autowired
    LogVerificationRepository logVerificationRepository;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private ITemplateService templateService;

    @Override
    public ResponseEntity<Object> login(LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var data  = usersRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(data);
        String refreshToken = jwtService.generateRefreshToken(data.getId());

        SignResponse response = new SignResponse(data.getEmail(), data.getRole().name(), token, refreshToken);

        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> register(RegisterRequest request) {
        ModelMapper modelMapper = new ModelMapper();
        Users userMapper = modelMapper.map(request, Users.class);
        userMapper.setRole(Role.USER);
        userMapper.setAccountName(request.getAccountName());
        userMapper.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userMapper.setBalance(new BigDecimal("100000.00"));
        var data = usersRepository.save(userMapper);

        //  ============ Log Account ========
        LogVerification verification = new LogVerification();
        verification.setCode(GenerateOtp.generateRandomNumber());
        verification.setUser(data);
        verification.setExpired(GenerateOtp.getExpiryDate());
        verification.setVerify(false);
        var saveLog = logVerificationRepository.save(verification);

//        ============= Send Email =================
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("otp", saveLog.getCode());
        templateVariables.put("name", data.getAccountName());
        String emailBody = templateService.processEmailTemplate("email_verification", templateVariables); // Pastikan nama template "email_verification" sesuai dengan nama file Anda

        // Gantikan mailService.sendEmail() dengan NotificationService
        EmailNotificationRequest emailRequest = new EmailNotificationRequest();
        emailRequest.setTo(data.getEmail());
        emailRequest.setSubject("Account Verification");
        emailRequest.setBody(emailBody);
        notificationService.sendEmailNotification(emailRequest);
        
        String token = jwtService.generateToken(data);

        RegisterResponse response = new RegisterResponse();
        response.setEmail(data.getEmail());
        response.setToken(token);

        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> logout(String email) {
        // Cari pengguna berdasarkan email DULU
        Optional<Users> userOptional = usersRepository.findByEmail(email); // <<< PENTING: Gunakan findByEmail
        if (userOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found.");
        }
        Users user = userOptional.get();

        // Reset saldo ke 100k
        user.setBalance(new BigDecimal("100000.00"));
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Logout successful. Your virtual balance has been reset to 100k.", null);
    }
    
}
