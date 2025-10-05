package com.soloproject.LegalPark.service.vehicle.users;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.dto.request.vehicle.VehicleUpdateRequest;
import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.dto.response.users.UserBasicResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.helper.VehicleResponseMapper;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserVehicleServiceImpl implements IUserVehicleService {

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    InfoAccount infoAccount;

    @Autowired
    VehicleResponseMapper vehicleResponseMapper;

    @Override
    public ResponseEntity<Object> UserRegisterVehicle(VehicleRequest request) {

        Users currentUser = infoAccount.get();

        if (currentUser == null)  {
            return ResponseHandler.generateResponseError(HttpStatus.UNAUTHORIZED, "FAILED", "User not authenticated. Please log in.");
        }

        // Check user AccountStatus
        if (currentUser.getAccountStatus() != AccountStatus.ACTIVE) {
            return ResponseHandler.generateResponseError(HttpStatus.FORBIDDEN, "FAILED", "Account is not active. Please verify your email first.");
        }


//        Contoh cara mengambil data
//        Merchant merchant = merchantOptional.get();

        // Check if the license plate already exists (if license_plate is unique)
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            return ResponseHandler.generateResponseError(HttpStatus.CONFLICT, "FAILED", "Vehicle with this license plate is already registered.");
        }

        ModelMapper modelMapper = new ModelMapper();
        Vehicle vehicleMapper = modelMapper.map(request, Vehicle.class);
        vehicleMapper.setLicensePlate(request.getLicensePlate());
        try {
            // Convert the string from the request to the VehicleType enum
            vehicleMapper.setType(VehicleType.valueOf(request.getType().toUpperCase())); // Change to UPPERCASE because enums are usually UPPERCASE
        } catch (IllegalArgumentException e) {
            // If the string type of the request does not match a valid enum name
            return ResponseHandler.generateResponseError(HttpStatus.BAD_REQUEST, "FAILED",
                    "Invalid vehicle type provided: " + request.getType() + ". Allowed types: " +
                            java.util.Arrays.toString(VehicleType.values()));
        }
        vehicleMapper.setOwner(currentUser);

        var data = vehicleRepository.save(vehicleMapper);

        VehicleResponse response = new VehicleResponse();
        response.setId(data.getId());
        response.setLicensePlate(data.getLicensePlate());
        response.setType(data.getType().name());

        if (data.getOwner() != null) {
            response.setOwner(modelMapper.map(data.getOwner(), UserBasicResponse.class));
        }


        return ResponseHandler.generateResponseSuccess(response);
    }

    @Override
    public ResponseEntity<Object> UserGetAllVehicle() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<VehicleResponse> responses = vehicles.stream()
                .map(vehicleResponseMapper::mapToVehicleResponse)
                .collect(Collectors.toList());
        return ResponseHandler.generateResponseSuccess(responses);
    }

    @Override
    public ResponseEntity<Object> UserGetVehicleById(String id) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(id);
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle not found with ID: " + id);
        }
        return ResponseHandler.generateResponseSuccess(vehicleResponseMapper.mapToVehicleResponse(vehicleOptional.get()));
    }

    @Override
    public ResponseEntity<Object> UserUpdateVehicle(String id, VehicleUpdateRequest request) {
        Users currentUser = infoAccount.get();

        if (currentUser == null)  {
            return ResponseHandler.generateResponseError(HttpStatus.UNAUTHORIZED, "FAILED", "User not authenticated. Please log in.");
        }

        // Check user AccountStatus
        if (currentUser.getAccountStatus() != AccountStatus.ACTIVE) {
            return ResponseHandler.generateResponseError(HttpStatus.FORBIDDEN, "FAILED", "Account is not active. Please verify your email first.");
        }

        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(id);

        // If the vehicle is not found, return the error NOT_FOUND
        if (vehicleOptional.isEmpty()) {
            return ResponseHandler.generateResponseError(HttpStatus.NOT_FOUND, "FAILED", "Vehicle with ID " + id + " not found.");
        }

        // Take the existing Vehicle object from Optional
        Vehicle existingVehicle = vehicleOptional.get();


        if (!existingVehicle.getOwner().getId().equals(currentUser.getId())) {
            return ResponseHandler.generateResponseError(HttpStatus.FORBIDDEN, "FAILED", "You are not authorized to update this vehicle.");
        }

        if(request.getType() != null) {
            existingVehicle.setType(VehicleType.valueOf(request.getType().toUpperCase()));
        }

        Vehicle updateVehicle = vehicleRepository.save(existingVehicle);

        VehicleResponse response = new VehicleResponse();
        response.setId(updateVehicle.getId());
        response.setLicensePlate(updateVehicle.getLicensePlate());
        response.setType(updateVehicle.getType().name());


        ModelMapper modelMapper = new ModelMapper();
        if (updateVehicle.getOwner() != null) {
            response.setOwner(modelMapper.map(updateVehicle.getOwner(), UserBasicResponse.class));
        }

        return ResponseHandler.generateResponseSuccess(response);
    }
}
