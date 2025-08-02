package com.soloproject.LegalPark.service.vehicle.admin;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.entity.Vehicle;
import com.soloproject.LegalPark.entity.VehicleType;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.helper.VehicleResponseMapper;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminVehicleServiceImpl implements IAdminVehicleService {

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    InfoAccount infoAccount;

    @Autowired
    VehicleResponseMapper vehicleResponseMapper;


    @Override
    public ResponseEntity<Object> adminRegisterVehicle(VehicleRequest request) {
        // Validasi ownerId
        if (request.getOwnerId() == null || request.getOwnerId().isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Owner ID is required for admin to create vehicle.");
        }
        Optional<Users> userOptional = usersRepository.findById(request.getOwnerId());
        if (userOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED", "Owner not found with ID: " + request.getOwnerId());
        }
        Users owner = userOptional.get();


        // Cek duplikasi plat nomor (masih penting, bahkan untuk admin)
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Vehicle with this license plate is already registered.");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        try {
            vehicle.setType(VehicleType.valueOf(request.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED",
                    "Invalid vehicle type provided: " + request.getType() + ". Allowed types: " +
                            java.util.Arrays.toString(VehicleType.values()));
        }
        vehicle.setOwner(owner);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return ResponseHandler.generateResponseSuccess(vehicleResponseMapper.mapToVehicleResponse(savedVehicle));
    }

    @Override
    public ResponseEntity<Object> adminGetAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<VehicleResponse> responses = vehicles.stream()
                .map(vehicleResponseMapper::mapToVehicleResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminGetVehicleById(String id) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(id);
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle not found with ID: " + id);
        }
        return ResponseHandler.generateResponseSuccess(vehicleResponseMapper.mapToVehicleResponse(vehicleOptional.get()));
    }

    @Override
    public ResponseEntity<Object> adminGetVehiclesByUserId(String userId) {
        Optional<Users> userOptional = usersRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "User not found with ID: " + userId);
        }
        List<Vehicle> vehicles = vehicleRepository.findByOwner (userOptional.get());
        List<VehicleResponse> responses = vehicles.stream()
                .map(vehicleResponseMapper::mapToVehicleResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> adminDeleteVehicle(String id) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(id);
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle not found with ID: " + id);
        }
        vehicleRepository.delete(vehicleOptional.get());
        return ResponseHandler.generateResponseSuccess("Vehicle with ID " + id + " has been deleted successfully.");
    }
}

