package com.soloproject.LegalPark.repository;

import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    Optional<Vehicle> findByLicensePlate(@Param("license_plate") String licensePlate);
    List<Vehicle> findByOwner(Users owner);
    Optional<Vehicle> findByOwnerId(@Param("owner_id") String owner);
}
