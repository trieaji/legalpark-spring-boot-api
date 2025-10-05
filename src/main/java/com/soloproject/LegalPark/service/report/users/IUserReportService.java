package com.soloproject.LegalPark.service.report.users;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface IUserReportService {
    /**
     * Retrieve parking transaction history for a specific user within a given time period.
     */
    ResponseEntity<Object> getUserParkingHistory(String userId, LocalDate startDate, LocalDate endDate);



    /**
     * Retrieve user parking and financial data summaries.
     */
    ResponseEntity<Object> getUserSummaryReport(String userId);

}
