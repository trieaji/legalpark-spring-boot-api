package com.soloproject.LegalPark.helper;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateOtp {
    public static String generateRandomNumber() {
        int randomNumber = ThreadLocalRandom.current().nextInt(1000, 10000);
        return String.valueOf(randomNumber);
    }

    public static LocalDateTime getExpiryDate() {
//        LocalDate today = LocalDate.now();
//        LocalDate expiryDate = today.plusDays(1);
//        return expiryDate;
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime expiryDate = today.plusDays(1);
        return expiryDate;
    }
}
