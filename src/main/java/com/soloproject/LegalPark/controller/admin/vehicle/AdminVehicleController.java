package com.soloproject.LegalPark.controller.admin.vehicle;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.service.vehicle.admin.IAdminVehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RestController("adminVehicleController") // <-- Tambahkan nama bean di sini. Jika ingin nama controller nya sama
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Vehicle API", description = "Endpoint for Vehicle Management by Admin")
public class AdminVehicleController {

    @Autowired
    IAdminVehicleService iAdminVehicleService;

    @PostMapping("/vehicle/register")
    public ResponseEntity<Object> adminRegisterVehicle(@RequestBody VehicleRequest request){
        return iAdminVehicleService.adminRegisterVehicle(request);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Object> adminGetAllVehicles() {
        return iAdminVehicleService.adminGetAllVehicles();
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<Object> adminGetVehicleById(@PathVariable("id") String id) {
        return iAdminVehicleService.adminGetVehicleById(id);
    }

    @GetMapping("/vehicle/by-user/{userId}")
    public ResponseEntity<Object> adminGetVehiclesByUserId(@PathVariable("userId") String userId) {
        return iAdminVehicleService.adminGetVehiclesByUserId(userId);
    }

    @DeleteMapping("/vehicle/{id}")
    public ResponseEntity<Object> adminDeleteVehicle(@PathVariable("id") String id){
        return iAdminVehicleService.adminDeleteVehicle(id);
    }
}
