package com.soloproject.LegalPark.service.report.admin;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface IAdminReportService {
    /**
     * Mengambil laporan pendapatan harian untuk seluruh sistem atau per merchant.
     */
    ResponseEntity<Object> getDailyRevenueReport(LocalDate date, String merchantCode);



    /**
     * Mengambil laporan status hunian semua slot parkir.

     */
    ResponseEntity<Object> getParkingSpotOccupancyReport(String merchantCode, String status);


}
