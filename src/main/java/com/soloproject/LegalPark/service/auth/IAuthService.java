package com.soloproject.LegalPark.service.auth;

import com.soloproject.LegalPark.dto.request.users.LoginRequest;
import com.soloproject.LegalPark.dto.request.users.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface IAuthService {
    ResponseEntity<Object> login(LoginRequest request);
    ResponseEntity<Object> register(RegisterRequest request);
    ResponseEntity<Object> logout(String email);
}
