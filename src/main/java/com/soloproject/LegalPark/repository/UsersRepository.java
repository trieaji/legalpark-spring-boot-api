package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    Optional<Users> findByEmail(@Param("email") String email);
    Optional<Users> findByAccountName(String accountName);
}
