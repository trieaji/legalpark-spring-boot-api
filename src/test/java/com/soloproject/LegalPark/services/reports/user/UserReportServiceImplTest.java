package com.soloproject.LegalPark.services.reports.user;

import com.soloproject.LegalPark.dto.response.report.UserParkingHistoryReportResponse;
import com.soloproject.LegalPark.dto.response.report.UserSummaryReportResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ReportResponseMapper;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.service.report.users.UserReportServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReportServiceImplTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private ParkingTransactionRepository parkingTransactionRepository;

    @Mock
    private ReportResponseMapper reportResponseMapper;

    @InjectMocks
    private UserReportServiceImpl userReportService;

    private MockedStatic<ResponseHandler> mockedResponseHandler;

    private String TEST_USER_ID = "user123";
    private String TEST_USER_NAME = "John Doe";
    private BigDecimal TEST_USER_BALANCE = new BigDecimal("150000.00");
    private Users mockUser;

    @BeforeEach
    void setUp() {
        mockedResponseHandler = mockStatic(ResponseHandler.class);

        // Menyesuaikan mocking ResponseHandler agar sesuai dengan implementasi Anda
        // Menggunakan konstruktor default ResponseHandler dan setter
        mockedResponseHandler.when(() -> ResponseHandler.generateResponseSuccess(any(Object.class)))
                .thenAnswer(invocation -> {
                    ResponseHandler rh = new ResponseHandler();
                    rh.setCode(HttpStatus.OK.value());
                    rh.setData(invocation.getArgument(0));
                    rh.setMessage("success"); // Pesan default untuk generateResponseSuccess(Object)
                    rh.setStatus(HttpStatus.OK);
                    return ResponseEntity.status(HttpStatus.OK).body(rh);
                });

        mockedResponseHandler.when(() -> ResponseHandler.generateResponseSuccess(any(HttpStatus.class), anyString(), any()))
                .thenAnswer(invocation -> {
                    ResponseHandler rh = new ResponseHandler();
                    rh.setCode(invocation.getArgument(0, HttpStatus.class).value());
                    rh.setStatus(invocation.getArgument(0, HttpStatus.class));
                    rh.setMessage(invocation.getArgument(1, String.class));
                    rh.setData(invocation.getArgument(2));
                    rh.setError(null);
                    return ResponseEntity.status(invocation.getArgument(0, HttpStatus.class)).body(rh);
                });

        mockedResponseHandler.when(() -> ResponseHandler.generateResponseError(any(HttpStatus.class), any(Object.class), anyString()))
                .thenAnswer(invocation -> {
                    ResponseHandler rh = new ResponseHandler();
                    rh.setCode(invocation.getArgument(0, HttpStatus.class).value());
                    rh.setStatus(invocation.getArgument(0, HttpStatus.class));
                    rh.setError(invocation.getArgument(1)); // Object error
                    rh.setMessage(invocation.getArgument(2, String.class)); // Message for user
                    return ResponseEntity.status(invocation.getArgument(0, HttpStatus.class)).body(rh);
                });


        // Setup mock user
        mockUser = new Users();
        mockUser.setId(TEST_USER_ID);
        mockUser.setAccountName(TEST_USER_NAME);
        mockUser.setBalance(TEST_USER_BALANCE);
        mockUser.setRole(Role.USER);
        mockUser.setAccountStatus(AccountStatus.ACTIVE);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("hashedpassword");
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        mockedResponseHandler.close();
    }

    // --- Helper Methods for Mock Entities ---
    private Vehicle createMockVehicle(Users owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("vehicle1");
        vehicle.setLicensePlate("B1234XYZ");
        vehicle.setType(VehicleType.CAR);
        vehicle.setOwner(owner);
        return vehicle;
    }

    private ParkingSpot createMockParkingSpot(Merchant merchant) {
        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setId("spot1");
        parkingSpot.setSpotNumber("A101");
        parkingSpot.setSpotType(SpotType.CAR);
        parkingSpot.setStatus(ParkingSpotStatus.OCCUPIED);
        parkingSpot.setFloor(1);
        parkingSpot.setMerchant(merchant);
        return parkingSpot;
    }

    private Merchant createMockMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId("merchant1");
        merchant.setMerchantName("Mall ABC");
        return merchant;
    }

    private ParkingTransaction createMockParkingTransaction(String id, Vehicle vehicle, ParkingSpot parkingSpot, LocalDateTime entry, LocalDateTime exit, BigDecimal cost, ParkingStatus pStatus, PaymentStatus payStatus) {
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setId(id);
        transaction.setVehicle(vehicle);
        transaction.setParkingSpot(parkingSpot);
        transaction.setEntryTime(entry);
        transaction.setExitTime(exit);
        transaction.setTotalCost(cost);
        transaction.setStatus(pStatus);
        transaction.setPaymentStatus(payStatus);
        transaction.setCreatedAt(entry.minusDays(1));
        transaction.setUpdatedAt(exit != null ? exit : entry);
        return transaction;
    }

    // --- Helper Methods for Mock DTO Responses (for mapper) ---
    private UserParkingHistoryReportResponse createMockUserParkingHistoryReportResponse(ParkingTransaction transaction) {
        UserParkingHistoryReportResponse response = new UserParkingHistoryReportResponse();
        response.setTransactionId(transaction.getId());
        response.setEntryTime(transaction.getEntryTime());
        response.setExitTime(transaction.getExitTime());
        response.setTotalCost(transaction.getTotalCost());
        response.setStatus(transaction.getStatus().name());
        response.setPaymentStatus(transaction.getPaymentStatus().name());
        // Simulasikan detail Vehicle dan ParkingSpot yang dipetakan oleh mapper
        response.setVehicle(new com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse());
        response.getVehicle().setLicensePlate(transaction.getVehicle().getLicensePlate());
        response.setParkingSpot(new com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse());
        response.getParkingSpot().setSpotNumber(transaction.getParkingSpot().getSpotNumber());
        return response;
    }

    // --- Tests for getUserParkingHistory ---

    @Test
    void getUserParkingHistory_Success_WithTransactions() {
        // Arrange
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999); // Akhir hari

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        Merchant merchant = createMockMerchant();
        Vehicle vehicle = createMockVehicle(mockUser);
        ParkingSpot parkingSpot = createMockParkingSpot(merchant);

        ParkingTransaction trans1 = createMockParkingTransaction("trans1", vehicle, parkingSpot,
                LocalDateTime.of(2023, 1, 10, 8, 0),
                LocalDateTime.of(2023, 1, 10, 10, 0),
                new BigDecimal("10000.00"), ParkingStatus.COMPLETED, PaymentStatus.PAID);

        ParkingTransaction trans2 = createMockParkingTransaction("trans2", vehicle, parkingSpot,
                LocalDateTime.of(2023, 1, 15, 9, 0),
                LocalDateTime.of(2023, 1, 15, 12, 0),
                new BigDecimal("15000.00"), ParkingStatus.COMPLETED, PaymentStatus.PAID);

        List<ParkingTransaction> mockTransactions = Arrays.asList(trans1, trans2);

        when(parkingTransactionRepository.findByVehicleOwnerIdAndEntryTimeBetween(
                eq(TEST_USER_ID), eq(startDateTime), eq(endDateTime)))
                .thenReturn(mockTransactions);

        // Mock mapper behavior
        when(reportResponseMapper.mapToUserParkingHistoryReportResponse(trans1)).thenReturn(createMockUserParkingHistoryReportResponse(trans1));
        when(reportResponseMapper.mapToUserParkingHistoryReportResponse(trans2)).thenReturn(createMockUserParkingHistoryReportResponse(trans2));

        // Act
        ResponseEntity<Object> response = userReportService.getUserParkingHistory(TEST_USER_ID, startDate, endDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.getMessage()); // Default message from generateResponseSuccess(Object)
        List<UserParkingHistoryReportResponse> history = (List<UserParkingHistoryReportResponse>) responseBody.getData();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("trans1", history.get(0).getTransactionId());
        assertEquals("trans2", history.get(1).getTransactionId());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findByVehicleOwnerIdAndEntryTimeBetween(eq(TEST_USER_ID), eq(startDateTime), eq(endDateTime));
        verify(reportResponseMapper, times(2)).mapToUserParkingHistoryReportResponse(any(ParkingTransaction.class));
    }

    @Test
    void getUserParkingHistory_UserNotFound() {
        // Arrange
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = userReportService.getUserParkingHistory(TEST_USER_ID, startDate, endDate);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError()); // Ini adalah 'error' object
        assertEquals("User not found with ID: " + TEST_USER_ID, responseBody.getMessage()); // Ini adalah 'message'

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(parkingTransactionRepository, reportResponseMapper);
    }

    @Test
    void getUserParkingHistory_NoTransactionsFound() {
        // Arrange
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(parkingTransactionRepository.findByVehicleOwnerIdAndEntryTimeBetween(
                eq(TEST_USER_ID), eq(startDateTime), eq(endDateTime)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Object> response = userReportService.getUserParkingHistory(TEST_USER_ID, startDate, endDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.getMessage()); // Default message
        List<UserParkingHistoryReportResponse> history = (List<UserParkingHistoryReportResponse>) responseBody.getData();
        assertNotNull(history);
        assertTrue(history.isEmpty());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findByVehicleOwnerIdAndEntryTimeBetween(eq(TEST_USER_ID), eq(startDateTime), eq(endDateTime));
        verifyNoInteractions(reportResponseMapper); // Mapper should not be called if no transactions
    }

    @Test
    void getUserParkingHistory_ExceptionThrown() {
        // Arrange
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        when(usersRepository.findById(TEST_USER_ID)).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                userReportService.getUserParkingHistory(TEST_USER_ID, startDate, endDate)
        );
        assertTrue(thrown.getMessage().contains("Database connection error"));

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(parkingTransactionRepository, reportResponseMapper);
    }

    // --- Tests for getUserSummaryReport ---

    @Test
    void getUserSummaryReport_Success_WithTransactions() {
        // Arrange
        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        Merchant merchant = createMockMerchant();
        Vehicle vehicle = createMockVehicle(mockUser);
        ParkingSpot parkingSpot = createMockParkingSpot(merchant);

        // Transactions for summary: 2 PAID, 1 PENDING, 1 FAILED
        ParkingTransaction trans1 = createMockParkingTransaction("trans1", vehicle, parkingSpot,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(5).plusHours(2),
                new BigDecimal("10000.00"), ParkingStatus.COMPLETED, PaymentStatus.PAID);
        ParkingTransaction trans2 = createMockParkingTransaction("trans2", vehicle, parkingSpot,
                LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(4).plusHours(3),
                new BigDecimal("15000.00"), ParkingStatus.COMPLETED, PaymentStatus.PAID);
        ParkingTransaction trans3 = createMockParkingTransaction("trans3", vehicle, parkingSpot,
                LocalDateTime.now().minusDays(3), null,
                null, ParkingStatus.ACTIVE, PaymentStatus.PENDING); // Active, pending payment
        ParkingTransaction trans4 = createMockParkingTransaction("trans4", vehicle, parkingSpot,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2).plusHours(1),
                new BigDecimal("5000.00"), ParkingStatus.COMPLETED, PaymentStatus.FAILED); // Failed payment

        List<ParkingTransaction> mockTransactions = Arrays.asList(trans1, trans2, trans3, trans4);
        when(parkingTransactionRepository.findByVehicleOwnerId(TEST_USER_ID)).thenReturn(mockTransactions);

        // Act
        ResponseEntity<Object> response = userReportService.getUserSummaryReport(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.getMessage());
        UserSummaryReportResponse summary = (UserSummaryReportResponse) responseBody.getData();
        assertNotNull(summary);

        assertEquals(TEST_USER_ID, summary.getUserId());
        assertEquals(TEST_USER_NAME, summary.getUserName());
        assertEquals(TEST_USER_BALANCE, summary.getCurrentBalance());
        assertEquals(4, summary.getTotalParkingSessions()); // All 4 transactions counted as sessions
        assertEquals(new BigDecimal("25000.00"), summary.getTotalCostSpent()); // Only PAID transactions counted for totalCost
        // TotalCostSpent calculation: 10000.00 (trans1) + 15000.00 (trans2) = 25000.00

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findByVehicleOwnerId(TEST_USER_ID);
        verifyNoInteractions(reportResponseMapper); // Mapper not used in summary report
    }

    @Test
    void getUserSummaryReport_UserNotFound() {
        // Arrange
        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = userReportService.getUserSummaryReport(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("User not found with ID: " + TEST_USER_ID, responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(parkingTransactionRepository, reportResponseMapper);
    }

    @Test
    void getUserSummaryReport_NoTransactionsFound() {
        // Arrange
        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(parkingTransactionRepository.findByVehicleOwnerId(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Object> response = userReportService.getUserSummaryReport(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.getMessage());
        UserSummaryReportResponse summary = (UserSummaryReportResponse) responseBody.getData();
        assertNotNull(summary);

        assertEquals(TEST_USER_ID, summary.getUserId());
        assertEquals(TEST_USER_NAME, summary.getUserName());
        assertEquals(TEST_USER_BALANCE, summary.getCurrentBalance());
        assertEquals(0, summary.getTotalParkingSessions());
        assertEquals(BigDecimal.ZERO, summary.getTotalCostSpent());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findByVehicleOwnerId(TEST_USER_ID);
        verifyNoInteractions(reportResponseMapper);
    }

    @Test
    void getUserSummaryReport_ExceptionThrown() {
        // Arrange
        when(usersRepository.findById(TEST_USER_ID)).thenThrow(new RuntimeException("Network error during user fetch"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                userReportService.getUserSummaryReport(TEST_USER_ID)
        );
        assertTrue(thrown.getMessage().contains("Network error during user fetch"));

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(parkingTransactionRepository, reportResponseMapper);
    }
}