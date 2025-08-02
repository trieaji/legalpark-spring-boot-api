package com.soloproject.LegalPark.dto.response.users;

public class SignResponse {
    private String email;
    private String role;
    private String token;
    private String refreshToken;

    public SignResponse(String email, String role, String token, String refreshToken) {
        this.email = email;
        this.role = role;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
