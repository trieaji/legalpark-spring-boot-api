package com.soloproject.LegalPark.service.vehicle.admin;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import org.springframework.http.ResponseEntity;

public interface IAdminVehicleService { // menangani siapa yang parkir (kendaraannya).
    ResponseEntity<Object> adminRegisterVehicle(VehicleRequest request);
    ResponseEntity<Object> adminGetAllVehicles();
    ResponseEntity<Object> adminGetVehicleById(String id);
    ResponseEntity<Object> adminGetVehiclesByUserId(String userId);
    ResponseEntity<Object> adminDeleteVehicle(String id);
}
