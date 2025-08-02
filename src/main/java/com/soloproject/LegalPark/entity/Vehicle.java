package com.soloproject.LegalPark.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "license_plate", unique = true, nullable = false, length = 20)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private VehicleType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private Users owner;

    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public Vehicle() {
    }

    public Vehicle(String id, String licensePlate, VehicleType type, Users owner, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.type = type;
        this.owner = owner;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public String getId() { // Getter untuk String ID
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    public Users getOwner() {
        return owner;
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

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public void setOwner(Users owner) {
        this.owner = owner;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}