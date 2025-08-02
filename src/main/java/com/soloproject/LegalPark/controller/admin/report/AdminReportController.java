package com.soloproject.LegalPark.controller.admin.report;

import com.soloproject.LegalPark.service.report.admin.IAdminReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin") // Contoh base URL untuk laporan admin
@Tag(name = "Admin Report API", description = "Endpoint untuk Admin mengelola dan melihat report parkir")
public class AdminReportController {

    @Autowired
    private IAdminReportService adminReportService;

    @PreAuthorize("hasRole('ADMIN')") // Contoh otorisasi
    @GetMapping("/report/revenue/daily")
    public ResponseEntity<Object> getDailyRevenueReport(
            @RequestParam LocalDate date,
            @RequestParam(required = false) String merchantCode) {

        return adminReportService.getDailyRevenueReport(date, merchantCode);
    }

    @PreAuthorize("hasRole('ADMIN')") // Contoh otorisasi
    @GetMapping("/report/occupancy")
    public ResponseEntity<Object> getParkingSpotOccupancyReport(
            @RequestParam(required = false) String merchantCode,
            @RequestParam(required = false) String status) {

        return adminReportService.getParkingSpotOccupancyReport(merchantCode, status);
    }
}