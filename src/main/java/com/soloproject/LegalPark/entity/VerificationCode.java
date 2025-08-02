package com.soloproject.LegalPark.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_codes")
public class VerificationCode {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private Users user;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private VehicleType vehicleType;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public VerificationCode() {
    }

    public VerificationCode(String id, Users user, String code, VehicleType vehicleType, LocalDateTime expiryTime, Boolean isUsed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.code = code;
        this.vehicleType = vehicleType;
        this.expiryTime = expiryTime;
        this.isUsed = isUsed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public String getId() { // Getter untuk String ID
        return id;
    }

    public Users getUser() {
        return user;
    }

    public String getCode() {
        return code;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public Boolean getUsed() {
        return isUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setId(String id) { // Setter untuk String ID
        this.id = id;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}