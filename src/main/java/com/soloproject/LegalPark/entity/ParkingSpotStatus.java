package com.soloproject.LegalPark.entity;

public enum ParkingSpotStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE, // Untuk slot yang rusak/tidak bisa digunakan
    RESERVED     // Jika ada fitur reservasi
}
