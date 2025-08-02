package com.soloproject.LegalPark.util;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretKeyGenerator {

    public static void main(String[] args) {
        // Untuk HS256, umumnya disarankan minimal 32 bytes (256 bits)
        // Untuk HS512, umumnya disarankan minimal 64 bytes (512 bits)
        int keyLengthBytes = 32; // Untuk HS256

        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[keyLengthBytes];
        secureRandom.nextBytes(keyBytes);

        // Encode ke Base64 agar mudah disimpan dan dibaca
        String secretKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Generated JWT Secret Key (Base64): " + secretKey);
        System.out.println("Key Length (bytes): " + keyBytes.length);
        System.out.println("Key Length (bits): " + (keyBytes.length * 8));
    }
}