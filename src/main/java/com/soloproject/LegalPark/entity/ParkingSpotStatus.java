package com.soloproject.LegalPark.entity;

public enum ParkingSpotStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE, // For broken/unusable slots
    RESERVED     // If there is a reservation feature
}
