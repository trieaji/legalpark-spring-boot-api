package com.soloproject.LegalPark.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_verification_codes")
public class PaymentVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private String code;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_transaction_id", unique = true)
    private ParkingTransaction parkingTransaction;


    public PaymentVerificationCode() {

    }


    public PaymentVerificationCode(Users user, String code, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.user = user;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isVerified = false; // Default
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public ParkingTransaction getParkingTransaction() {
        return parkingTransaction;
    }

    public void setParkingTransaction(ParkingTransaction parkingTransaction) {
        this.parkingTransaction = parkingTransaction;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
