package com.soloproject.LegalPark.service.vehicle.users;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.dto.request.vehicle.VehicleUpdateRequest;
import org.springframework.http.ResponseEntity;

public interface IUserVehicleService { // menangani siapa yang parkir (kendaraannya).
    ResponseEntity<Object> UserRegisterVehicle(VehicleRequest request);
    ResponseEntity<Object> UserGetAllVehicle();
    ResponseEntity<Object> UserGetVehicleById(String id);
    ResponseEntity<Object> UserUpdateVehicle(String id, VehicleUpdateRequest request);
}
