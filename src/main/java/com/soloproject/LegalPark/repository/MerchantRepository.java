package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByMerchantCode(@Param("merchant_code") String merchantCode);
}
