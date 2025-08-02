package com.soloproject.LegalPark.controller.users.vehicle;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.dto.request.vehicle.VehicleUpdateRequest;
import com.soloproject.LegalPark.service.vehicle.users.IUserVehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Vehicle Controller", description = "Vehicle Service")
public class UserVehicleController {

    @Autowired
    IUserVehicleService iUserVehicleService;

    @PostMapping("/vehicle/register")
    public ResponseEntity<Object> UserRegisterVehicle(@RequestBody VehicleRequest request){
        return iUserVehicleService.UserRegisterVehicle(request);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Object> UserGetAllVehicle() {
        return iUserVehicleService.UserGetAllVehicle();
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<Object> UserGetVehicleById(@PathVariable("id") String id) {
        return iUserVehicleService.UserGetVehicleById(id);
    }

    @PatchMapping("/vehicle/{id}")
    public ResponseEntity<Object> UserUpdateVehicle(@PathVariable("id") String id, @RequestBody VehicleUpdateRequest request) {
        return iUserVehicleService.UserUpdateVehicle(id, request);
    }
}
