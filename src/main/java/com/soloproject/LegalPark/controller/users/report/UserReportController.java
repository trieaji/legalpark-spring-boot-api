package com.soloproject.LegalPark.controller.users.report;

import com.soloproject.LegalPark.service.report.users.IUserReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Parking Report API", description = "Endpoint untuk Pengguna melihat report parkir mereka")
public class UserReportController {

    @Autowired
    private IUserReportService userReportService;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/report/{userId}/history")
    public ResponseEntity<Object> getUserParkingHistory(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return userReportService.getUserParkingHistory(userId, startDate, endDate);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/report/{userId}/summary")
    public ResponseEntity<Object> getUserSummaryReport(@PathVariable String userId) {

        return userReportService.getUserSummaryReport(userId);
    }
}
