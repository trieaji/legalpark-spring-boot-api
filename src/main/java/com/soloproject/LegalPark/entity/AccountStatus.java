package com.soloproject.LegalPark.entity;

public enum AccountStatus {
    PENDING_VERIFICATION, // Akun baru terdaftar, menunggu verifikasi email
    ACTIVE,               // Akun aktif, email sudah terverifikasi, fitur pembayaran aktif
    INACTIVE,             // Akun dinonaktifkan (misal karena logout atau admin)
    SUSPENDED,            // Akun ditangguhkan
    BLOCKED               // Akun diblokir
}
