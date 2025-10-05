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

    // Characters that can be used for short codes
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 8; // Panjang kode pendek, bisa disesuaikan
    private static final SecureRandom random = new SecureRandom();

    // Constructor for dependency injection MerchantRepository
    @Autowired
    public CodeGeneratorUtil(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }


//     Generates a unique short alphanumeric code for merchants.
    public String generateUniqueMerchantShortCode() {
        String generatedCode;
        boolean isUnique = false;
        do {
            StringBuilder shortCodeBuilder = new StringBuilder(SHORT_CODE_LENGTH);
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
                shortCodeBuilder.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
            }
            generatedCode = shortCodeBuilder.toString();


            Optional<Merchant> existingMerchant = merchantRepository.findByMerchantCode(generatedCode);
            if (existingMerchant.isEmpty()) {
                isUnique = true;
            }

        } while (!isUnique);

        return generatedCode;
    }

}
