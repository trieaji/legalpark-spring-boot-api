package com.soloproject.LegalPark.util;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretKeyGenerator {

    public static void main(String[] args) {

        int keyLengthBytes = 32;

        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[keyLengthBytes];
        secureRandom.nextBytes(keyBytes);

        // Encode to Base64 for easy storage and reading
        String secretKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Generated JWT Secret Key (Base64): " + secretKey);
        System.out.println("Key Length (bytes): " + keyBytes.length);
        System.out.println("Key Length (bits): " + (keyBytes.length * 8));
    }
}