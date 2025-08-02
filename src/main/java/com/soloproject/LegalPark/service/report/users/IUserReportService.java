package com.soloproject.LegalPark.service.report.users;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface IUserReportService {
    /**
     * Mengambil riwayat transaksi parkir untuk user tertentu dalam periode waktu.
     */
    ResponseEntity<Object> getUserParkingHistory(String userId, LocalDate startDate, LocalDate endDate);



    /**
     * Mengambil ringkasan data parkir dan keuangan user.
     */
    ResponseEntity<Object> getUserSummaryReport(String userId);

}
