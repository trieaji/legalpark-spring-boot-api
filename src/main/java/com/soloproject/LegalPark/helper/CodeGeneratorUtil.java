package com.soloproject.LegalPark.helper;

import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Optional;

@Component
public class CodeGeneratorUtil {
    private final MerchantRepository merchantRepository; // Inject MerchantRepository

    // Karakter yang bisa digunakan untuk kode pendek
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 8; // Panjang kode pendek, bisa disesuaikan
    private static final SecureRandom random = new SecureRandom();

    // Constructor untuk dependency injection MerchantRepository
    @Autowired
    public CodeGeneratorUtil(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }


//     Menghasilkan kode alfanumerik pendek yang unik untuk merchant.
//     Kode ini dijamin unik dengan memeriksa di database.
//     @return Kode merchant pendek yang unik

    public String generateUniqueMerchantShortCode() {
        String generatedCode;
        boolean isUnique = false;
        do {
            StringBuilder shortCodeBuilder = new StringBuilder(SHORT_CODE_LENGTH);
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
                shortCodeBuilder.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
            }
            generatedCode = shortCodeBuilder.toString();

            // Periksa keunikan di database
            // Pastikan findByMerchantCode ada di MerchantRepository dan mencari kode pendek
            Optional<Merchant> existingMerchant = merchantRepository.findByMerchantCode(generatedCode);
            if (existingMerchant.isEmpty()) {
                isUnique = true;
            }
            // Jika tidak unik, loop akan berlanjut dan menghasilkan kode baru
        } while (!isUnique);

        return generatedCode;
    }

    // Kau bisa menambahkan metode generate kode unik lain di sini kalau kau butuh
    // Misalnya, untuk OTP, kode promo, dll
}
