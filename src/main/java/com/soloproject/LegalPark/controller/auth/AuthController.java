package com.soloproject.LegalPark.controller.auth;

import com.soloproject.LegalPark.dto.request.users.AccountVerification;
import com.soloproject.LegalPark.dto.request.users.LoginRequest;
import com.soloproject.LegalPark.dto.request.users.RegisterRequest;
import com.soloproject.LegalPark.service.auth.IAuthService;
import com.soloproject.LegalPark.service.users.IUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "Authentication Service")
public class AuthController {

    @Autowired
    IAuthService iAuthService;

    @Autowired
    IUsersService iUsersService;

    @Operation(summary = "Login Service", description = "authentication service")
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request){
        return iAuthService.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request){
        return iAuthService.register(request);
    }

    @Operation(
            summary = "Secure data",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/verification-account")
    public ResponseEntity<Object> verificationAccount(@RequestBody AccountVerification request){
        return iUsersService.verificationAccount(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(Authentication authentication) {
        String email = authentication.getName();
        return iAuthService.logout(email);
    }
    
}
