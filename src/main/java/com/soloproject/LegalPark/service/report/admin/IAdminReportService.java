package com.soloproject.LegalPark.service.report.admin;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface IAdminReportService {
    /**
     * Retrieve daily income reports for the entire system or per merchant.
     */
    ResponseEntity<Object> getDailyRevenueReport(LocalDate date, String merchantCode);



    /**
     * Retrieve occupancy status reports for all parking slots.

     */
    ResponseEntity<Object> getParkingSpotOccupancyReport(String merchantCode, String status);


}
