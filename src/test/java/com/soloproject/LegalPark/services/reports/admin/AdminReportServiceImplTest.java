package com.soloproject.LegalPark.services.reports.admin;

import com.soloproject.LegalPark.dto.response.report.AdminDailyRevenueReportResponse;
import com.soloproject.LegalPark.dto.response.report.AdminParkingSpotOccupancyReportResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.ParkingSpotRepository;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.service.report.admin.AdminReportServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    @Mock
    private ParkingTransactionRepository parkingTransactionRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private ModelMapper modelMapper; // Meskipun tidak digunakan langsung untuk mapping di service, perlu di-mock

    @InjectMocks
    private AdminReportServiceImpl adminReportService;

    private MockedStatic<ResponseHandler> mockedResponseHandler;

    private String TEST_MERCHANT_CODE = "MERC001";
    private String TEST_MERCHANT_NAME = "Merchant A";
    private String TEST_MERCHANT_ID = "merch123";
    private Merchant mockMerchant;

    private String TEST_USER_ID = "user123";
    private String TEST_USER_NAME = "Test User";
    private Users mockUser;

    @BeforeEach
    void setUp() {
        mockedResponseHandler = mockStatic(ResponseHandler.class);

        // Menyesuaikan mocking ResponseHandler agar sesuai dengan implementasi Anda
        mockedResponseHandler.when(() -> ResponseHandler.generateResponseSuccess(any(Object.class)))
                .thenAnswer(invocation -> {
                    ResponseHandler rh = new ResponseHandler();
                    rh.setCode(HttpStatus.OK.value());
                    rh.setData(invocation.getArgument(0));
                    rh.setMessage("success"); // Pesan default dari generateResponseSuccess(Object)
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
                    rh.setError(invocation.getArgument(1));
                    rh.setMessage(invocation.getArgument(2, String.class));
                    return ResponseEntity.status(invocation.getArgument(0, HttpStatus.class)).body(rh);
                });

        // Setup mock entities
        mockMerchant = new Merchant();
        mockMerchant.setId(TEST_MERCHANT_ID);
        mockMerchant.setMerchantCode(TEST_MERCHANT_CODE);
        mockMerchant.setMerchantName(TEST_MERCHANT_NAME);

        mockUser = new Users();
        mockUser.setId(TEST_USER_ID);
        mockUser.setAccountName(TEST_USER_NAME);
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

    private ParkingSpot createMockParkingSpot(Merchant merchant, String spotId, String spotNumber, ParkingSpotStatus status) {
        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setId(spotId);
        parkingSpot.setSpotNumber(spotNumber);
        parkingSpot.setSpotType(SpotType.CAR);
        parkingSpot.setStatus(status);
        parkingSpot.setFloor(1);
        parkingSpot.setMerchant(merchant);
        return parkingSpot;
    }

    private ParkingTransaction createMockParkingTransaction(String id, Vehicle vehicle, ParkingSpot parkingSpot, LocalDateTime entry, LocalDateTime exit, BigDecimal cost, PaymentStatus payStatus) {
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setId(id);
        transaction.setVehicle(vehicle);
        transaction.setParkingSpot(parkingSpot);
        transaction.setEntryTime(entry);
        transaction.setExitTime(exit);
        transaction.setTotalCost(cost);
        transaction.setStatus(ParkingStatus.COMPLETED); // Asumsi completed for revenue report
        transaction.setPaymentStatus(payStatus);
        return transaction;
    }

    // --- Tests for getDailyRevenueReport ---

    @Test
    void getDailyRevenueReport_AllMerchants_Success() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 7, 26);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Merchant merchantB = new Merchant();
        merchantB.setId("merch456");
        merchantB.setMerchantCode("MERC002");
        merchantB.setMerchantName("Merchant B");

        ParkingSpot spot1A = createMockParkingSpot(mockMerchant, "spot1A", "A01", ParkingSpotStatus.OCCUPIED);
        ParkingSpot spot2B = createMockParkingSpot(merchantB, "spot2B", "B01", ParkingSpotStatus.OCCUPIED);

        ParkingTransaction trans1 = createMockParkingTransaction("trans1", createMockVehicle(mockUser), spot1A,
                LocalDateTime.of(2023, 7, 26, 8, 0), LocalDateTime.of(2023, 7, 26, 10, 0),
                new BigDecimal("10000.00"), PaymentStatus.PAID);
        ParkingTransaction trans2 = createMockParkingTransaction("trans2", createMockVehicle(mockUser), spot2B,
                LocalDateTime.of(2023, 7, 26, 9, 0), LocalDateTime.of(2023, 7, 26, 11, 0),
                new BigDecimal("12000.00"), PaymentStatus.PAID);
        ParkingTransaction trans3 = createMockParkingTransaction("trans3", createMockVehicle(mockUser), spot1A,
                LocalDateTime.of(2023, 7, 26, 13, 0), LocalDateTime.of(2023, 7, 26, 14, 0),
                new BigDecimal("5000.00"), PaymentStatus.PAID);

        List<ParkingTransaction> paidTransactions = Arrays.asList(trans1, trans2, trans3);

        when(parkingTransactionRepository.findPaidTransactionsByExitTimeBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(paidTransactions);

        // Act
        ResponseEntity<Object> response = adminReportService.getDailyRevenueReport(date, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.getMessage()); // Default success message
        List<AdminDailyRevenueReportResponse> reports = (List<AdminDailyRevenueReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertEquals(2, reports.size()); // Two merchants

        AdminDailyRevenueReportResponse reportMERC001 = reports.stream()
                .filter(r -> r.getMerchantCode().equals(TEST_MERCHANT_CODE)).findFirst().orElse(null);
        assertNotNull(reportMERC001);
        assertEquals(date, reportMERC001.getDate());
        assertEquals(TEST_MERCHANT_NAME, reportMERC001.getMerchantName());
        assertEquals(new BigDecimal("15000.00"), reportMERC001.getTotalRevenue()); // 10000 + 5000
        assertEquals(2, reportMERC001.getTotalPaidTransactions());

        AdminDailyRevenueReportResponse reportMERC002 = reports.stream()
                .filter(r -> r.getMerchantCode().equals("MERC002")).findFirst().orElse(null);
        assertNotNull(reportMERC002);
        assertEquals(date, reportMERC002.getDate());
        assertEquals("Merchant B", reportMERC002.getMerchantName());
        assertEquals(new BigDecimal("12000.00"), reportMERC002.getTotalRevenue());
        assertEquals(1, reportMERC002.getTotalPaidTransactions());

        verify(parkingTransactionRepository).findPaidTransactionsByExitTimeBetween(eq(startOfDay), eq(endOfDay));
        verify(merchantRepository, never()).findByMerchantCode(anyString()); // Should not call for all merchants
    }

    @Test
    void getDailyRevenueReport_SpecificMerchant_Success() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 7, 26);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        when(merchantRepository.findByMerchantCode(TEST_MERCHANT_CODE)).thenReturn(Optional.of(mockMerchant));

        ParkingSpot spot1 = createMockParkingSpot(mockMerchant, "spot1", "A01", ParkingSpotStatus.OCCUPIED);
        ParkingTransaction trans1 = createMockParkingTransaction("trans1", createMockVehicle(mockUser), spot1,
                LocalDateTime.of(2023, 7, 26, 8, 0), LocalDateTime.of(2023, 7, 26, 10, 0),
                new BigDecimal("10000.00"), PaymentStatus.PAID);
        ParkingTransaction trans2 = createMockParkingTransaction("trans2", createMockVehicle(mockUser), spot1,
                LocalDateTime.of(2023, 7, 26, 9, 0), LocalDateTime.of(2023, 7, 26, 11, 0),
                new BigDecimal("15000.00"), PaymentStatus.PAID);

        List<ParkingTransaction> paidTransactions = Arrays.asList(trans1, trans2);

        when(parkingTransactionRepository.findPaidTransactionsByExitTimeBetweenAndMerchantCode(
                eq(startOfDay), eq(endOfDay), eq(TEST_MERCHANT_CODE)))
                .thenReturn(paidTransactions);

        // Act
        ResponseEntity<Object> response = adminReportService.getDailyRevenueReport(date, TEST_MERCHANT_CODE);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Daily revenue report retrieved successfully for merchant " + TEST_MERCHANT_NAME + ".", responseBody.getMessage());
        List<AdminDailyRevenueReportResponse> reports = (List<AdminDailyRevenueReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertEquals(1, reports.size());

        AdminDailyRevenueReportResponse report = reports.get(0);
        assertEquals(date, report.getDate());
        assertEquals(TEST_MERCHANT_CODE, report.getMerchantCode());
        assertEquals(TEST_MERCHANT_NAME, report.getMerchantName());
        assertEquals(new BigDecimal("25000.00"), report.getTotalRevenue()); // 10000 + 15000
        assertEquals(2, report.getTotalPaidTransactions());

        verify(merchantRepository).findByMerchantCode(TEST_MERCHANT_CODE);
        verify(parkingTransactionRepository).findPaidTransactionsByExitTimeBetweenAndMerchantCode(
                eq(startOfDay), eq(endOfDay), eq(TEST_MERCHANT_CODE));
        verify(parkingTransactionRepository, never()).findPaidTransactionsByExitTimeBetween(any(), any());
    }

    @Test
    void getDailyRevenueReport_SpecificMerchant_NotFound() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 7, 26);
        when(merchantRepository.findByMerchantCode(TEST_MERCHANT_CODE)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = adminReportService.getDailyRevenueReport(date, TEST_MERCHANT_CODE);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Merchant not found with code: " + TEST_MERCHANT_CODE, responseBody.getMessage());

        verify(merchantRepository).findByMerchantCode(TEST_MERCHANT_CODE);
        verifyNoInteractions(parkingTransactionRepository);
    }

    @Test
    void getDailyRevenueReport_NoRevenue_AllMerchants() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 7, 26);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        when(parkingTransactionRepository.findPaidTransactionsByExitTimeBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Object> response = adminReportService.getDailyRevenueReport(date, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("No revenue recorded for " + date + ".", responseBody.getMessage());
        List<AdminDailyRevenueReportResponse> reports = (List<AdminDailyRevenueReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertTrue(reports.isEmpty());

        verify(parkingTransactionRepository).findPaidTransactionsByExitTimeBetween(eq(startOfDay), eq(endOfDay));
    }

    @Test
    void getDailyRevenueReport_NoRevenue_SpecificMerchant() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 7, 26);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        when(merchantRepository.findByMerchantCode(TEST_MERCHANT_CODE)).thenReturn(Optional.of(mockMerchant));
        when(parkingTransactionRepository.findPaidTransactionsByExitTimeBetweenAndMerchantCode(
                eq(startOfDay), eq(endOfDay), eq(TEST_MERCHANT_CODE)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Object> response = adminReportService.getDailyRevenueReport(date, TEST_MERCHANT_CODE);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("No revenue recorded for " + date + " at merchant " + TEST_MERCHANT_CODE + ".", responseBody.getMessage());
        List<AdminDailyRevenueReportResponse> reports = (List<AdminDailyRevenueReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertTrue(reports.isEmpty());

        verify(merchantRepository).findByMerchantCode(TEST_MERCHANT_CODE);
        verify(parkingTransactionRepository).findPaidTransactionsByExitTimeBetweenAndMerchantCode(
                eq(startOfDay), eq(endOfDay), eq(TEST_MERCHANT_CODE));
    }

    @Test
    void getDailyRevenueReport_ExceptionThrown() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 7, 26);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        when(parkingTransactionRepository.findPaidTransactionsByExitTimeBetween(any(), any()))
                .thenThrow(new RuntimeException("DB Connection Down"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                adminReportService.getDailyRevenueReport(date, null)
        );
        assertTrue(thrown.getMessage().contains("DB Connection Down"));

        verify(parkingTransactionRepository).findPaidTransactionsByExitTimeBetween(eq(startOfDay), eq(endOfDay));
    }

    // --- Tests for getParkingSpotOccupancyReport ---

    @Test
    void getParkingSpotOccupancyReport_AllMerchants_AllStatuses_Success() {
        // Arrange
        Merchant merchantB = new Merchant();
        merchantB.setId("merch456");
        merchantB.setMerchantCode("MERC002");
        merchantB.setMerchantName("Merchant B");

        ParkingSpot spot1 = createMockParkingSpot(mockMerchant, "spot1", "A01", ParkingSpotStatus.AVAILABLE);
        ParkingSpot spot2 = createMockParkingSpot(mockMerchant, "spot2", "A02", ParkingSpotStatus.OCCUPIED);
        ParkingSpot spot3 = createMockParkingSpot(merchantB, "spot3", "B01", ParkingSpotStatus.MAINTENANCE);
        ParkingSpot spot4 = createMockParkingSpot(merchantB, "spot4", "B02", ParkingSpotStatus.OCCUPIED);

        List<ParkingSpot> allSpots = Arrays.asList(spot1, spot2, spot3, spot4);
        when(parkingSpotRepository.findAll()).thenReturn(allSpots);

        // Mock active transaction for OCCUPIED spots
        Vehicle vehicleForSpot2 = createMockVehicle(mockUser);
        ParkingTransaction activeTransSpot2 = new ParkingTransaction();
        activeTransSpot2.setVehicle(vehicleForSpot2);
        activeTransSpot2.setParkingSpot(spot2);
        activeTransSpot2.setStatus(ParkingStatus.ACTIVE);

        Vehicle vehicleForSpot4 = createMockVehicle(mockUser);
        ParkingTransaction activeTransSpot4 = new ParkingTransaction();
        activeTransSpot4.setVehicle(vehicleForSpot4);
        activeTransSpot4.setParkingSpot(spot4);
        activeTransSpot4.setStatus(ParkingStatus.ACTIVE);

        when(parkingTransactionRepository.findByParkingSpotAndStatus(eq(spot2), eq(ParkingStatus.ACTIVE)))
                .thenReturn(Optional.of(activeTransSpot2));
        when(parkingTransactionRepository.findByParkingSpotAndStatus(eq(spot4), eq(ParkingStatus.ACTIVE)))
                .thenReturn(Optional.of(activeTransSpot4));

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Parking spot occupancy report retrieved successfully.", responseBody.getMessage());
        List<AdminParkingSpotOccupancyReportResponse> reports = (List<AdminParkingSpotOccupancyReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertEquals(4, reports.size());

        // Verify details for an OCCUPIED spot
        AdminParkingSpotOccupancyReportResponse occupiedSpotReport = reports.stream()
                .filter(r -> r.getSpotId().equals("spot2")).findFirst().orElse(null);
        assertNotNull(occupiedSpotReport);
        assertEquals("OCCUPIED", occupiedSpotReport.getCurrentStatus());
        assertEquals(mockMerchant.getMerchantCode(), occupiedSpotReport.getMerchantCode());
        assertEquals("B1234XYZ", occupiedSpotReport.getCurrentVehicleLicensePlate());
        assertEquals("CAR", occupiedSpotReport.getCurrentVehicleType());
        assertEquals(TEST_USER_NAME, occupiedSpotReport.getCurrentOccupantUserName());

        // Verify details for an AVAILABLE spot
        AdminParkingSpotOccupancyReportResponse availableSpotReport = reports.stream()
                .filter(r -> r.getSpotId().equals("spot1")).findFirst().orElse(null);
        assertNotNull(availableSpotReport);
        assertEquals("AVAILABLE", availableSpotReport.getCurrentStatus());
        assertNull(availableSpotReport.getCurrentVehicleLicensePlate());
        assertNull(availableSpotReport.getCurrentOccupantUserName());

        verify(parkingSpotRepository).findAll();
        verify(parkingTransactionRepository, times(2)).findByParkingSpotAndStatus(any(ParkingSpot.class), eq(ParkingStatus.ACTIVE));
    }

    @Test
    void getParkingSpotOccupancyReport_SpecificMerchant_AllStatuses_Success() {
        // Arrange
        when(merchantRepository.findByMerchantCode(TEST_MERCHANT_CODE)).thenReturn(Optional.of(mockMerchant));

        ParkingSpot spot1 = createMockParkingSpot(mockMerchant, "spot1", "A01", ParkingSpotStatus.AVAILABLE);
        ParkingSpot spot2 = createMockParkingSpot(mockMerchant, "spot2", "A02", ParkingSpotStatus.OCCUPIED);
        List<ParkingSpot> merchantSpots = Arrays.asList(spot1, spot2);
        when(parkingSpotRepository.findByMerchant(eq(mockMerchant))).thenReturn(merchantSpots);

        // Mock active transaction for OCCUPIED spot
        Vehicle vehicleForSpot2 = createMockVehicle(mockUser);
        ParkingTransaction activeTransSpot2 = new ParkingTransaction();
        activeTransSpot2.setVehicle(vehicleForSpot2);
        activeTransSpot2.setParkingSpot(spot2);
        activeTransSpot2.setStatus(ParkingStatus.ACTIVE);
        when(parkingTransactionRepository.findByParkingSpotAndStatus(eq(spot2), eq(ParkingStatus.ACTIVE)))
                .thenReturn(Optional.of(activeTransSpot2));

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(TEST_MERCHANT_CODE, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Parking spot occupancy report retrieved successfully.", responseBody.getMessage());
        List<AdminParkingSpotOccupancyReportResponse> reports = (List<AdminParkingSpotOccupancyReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertEquals(2, reports.size());

        verify(merchantRepository).findByMerchantCode(TEST_MERCHANT_CODE);
        verify(parkingSpotRepository).findByMerchant(eq(mockMerchant));
        verify(parkingTransactionRepository).findByParkingSpotAndStatus(eq(spot2), eq(ParkingStatus.ACTIVE));
    }

    @Test
    void getParkingSpotOccupancyReport_AllMerchants_SpecificStatus_Success() {
        // Arrange
        ParkingSpot spot1 = createMockParkingSpot(mockMerchant, "spot1", "A01", ParkingSpotStatus.OCCUPIED);
        ParkingSpot spot2 = createMockParkingSpot(mockMerchant, "spot2", "B01", ParkingSpotStatus.OCCUPIED); // Another merchant
        List<ParkingSpot> occupiedSpots = Arrays.asList(spot1, spot2);
        when(parkingSpotRepository.findByStatus(eq(ParkingSpotStatus.OCCUPIED))).thenReturn(occupiedSpots);

        Vehicle vehicle = createMockVehicle(mockUser);
        ParkingTransaction activeTrans1 = new ParkingTransaction();
        activeTrans1.setVehicle(vehicle);
        activeTrans1.setParkingSpot(spot1);
        activeTrans1.setStatus(ParkingStatus.ACTIVE);
        when(parkingTransactionRepository.findByParkingSpotAndStatus(eq(spot1), eq(ParkingStatus.ACTIVE)))
                .thenReturn(Optional.of(activeTrans1));

        ParkingTransaction activeTrans2 = new ParkingTransaction();
        activeTrans2.setVehicle(vehicle);
        activeTrans2.setParkingSpot(spot2);
        activeTrans2.setStatus(ParkingStatus.ACTIVE);
        when(parkingTransactionRepository.findByParkingSpotAndStatus(eq(spot2), eq(ParkingStatus.ACTIVE)))
                .thenReturn(Optional.of(activeTrans2));

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(null, "OCCUPIED");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Parking spot occupancy report retrieved successfully.", responseBody.getMessage());
        List<AdminParkingSpotOccupancyReportResponse> reports = (List<AdminParkingSpotOccupancyReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertEquals(2, reports.size());
        assertTrue(reports.stream().allMatch(r -> r.getCurrentStatus().equals("OCCUPIED")));

        verify(parkingSpotRepository).findByStatus(eq(ParkingSpotStatus.OCCUPIED));
        verify(parkingTransactionRepository, times(2)).findByParkingSpotAndStatus(any(ParkingSpot.class), eq(ParkingStatus.ACTIVE));
    }

    @Test
    void getParkingSpotOccupancyReport_SpecificMerchant_SpecificStatus_Success() {
        // Arrange
        when(merchantRepository.findByMerchantCode(TEST_MERCHANT_CODE)).thenReturn(Optional.of(mockMerchant));

        ParkingSpot spot1 = createMockParkingSpot(mockMerchant, "spot1", "A01", ParkingSpotStatus.OCCUPIED);
        List<ParkingSpot> occupiedSpots = Collections.singletonList(spot1);
        when(parkingSpotRepository.findByMerchantAndStatus(eq(mockMerchant), eq(ParkingSpotStatus.OCCUPIED)))
                .thenReturn(occupiedSpots);

        Vehicle vehicle = createMockVehicle(mockUser);
        ParkingTransaction activeTrans1 = new ParkingTransaction();
        activeTrans1.setVehicle(vehicle);
        activeTrans1.setParkingSpot(spot1);
        activeTrans1.setStatus(ParkingStatus.ACTIVE);
        when(parkingTransactionRepository.findByParkingSpotAndStatus(eq(spot1), eq(ParkingStatus.ACTIVE)))
                .thenReturn(Optional.of(activeTrans1));

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(TEST_MERCHANT_CODE, "OCCUPIED");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Parking spot occupancy report retrieved successfully.", responseBody.getMessage());
        List<AdminParkingSpotOccupancyReportResponse> reports = (List<AdminParkingSpotOccupancyReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals("spot1", reports.get(0).getSpotId());
        assertEquals("OCCUPIED", reports.get(0).getCurrentStatus());

        verify(merchantRepository).findByMerchantCode(TEST_MERCHANT_CODE);
        verify(parkingSpotRepository).findByMerchantAndStatus(eq(mockMerchant), eq(ParkingSpotStatus.OCCUPIED));
        verify(parkingTransactionRepository).findByParkingSpotAndStatus(eq(spot1), eq(ParkingStatus.ACTIVE));
    }


    @Test
    void getParkingSpotOccupancyReport_MerchantNotFound() {
        // Arrange
        when(merchantRepository.findByMerchantCode(TEST_MERCHANT_CODE)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(TEST_MERCHANT_CODE, null);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Merchant not found with code: " + TEST_MERCHANT_CODE, responseBody.getMessage());

        verify(merchantRepository).findByMerchantCode(TEST_MERCHANT_CODE);
        verifyNoInteractions(parkingSpotRepository, parkingTransactionRepository);
    }

    @Test
    void getParkingSpotOccupancyReport_InvalidStatus() {
        // Arrange
        String invalidStatus = "NON_EXISTENT_STATUS";

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(null, invalidStatus);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Invalid parking spot status: " + invalidStatus, responseBody.getMessage());

        verifyNoInteractions(merchantRepository, parkingSpotRepository, parkingTransactionRepository);
    }

    @Test
    void getParkingSpotOccupancyReport_NoSpotsFound() {
        // Arrange
        when(parkingSpotRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Object> response = adminReportService.getParkingSpotOccupancyReport(null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("No parking spots found matching criteria.", responseBody.getMessage());
        List<AdminParkingSpotOccupancyReportResponse> reports = (List<AdminParkingSpotOccupancyReportResponse>) responseBody.getData();
        assertNotNull(reports);
        assertTrue(reports.isEmpty());

        verify(parkingSpotRepository).findAll();
        verifyNoInteractions(parkingTransactionRepository);
    }

    @Test
    void getParkingSpotOccupancyReport_ExceptionThrown() {
        // Arrange
        when(parkingSpotRepository.findAll()).thenThrow(new RuntimeException("DB Error during spot fetch"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                adminReportService.getParkingSpotOccupancyReport(null, null)
        );
        assertTrue(thrown.getMessage().contains("DB Error during spot fetch"));

        verify(parkingSpotRepository).findAll();
        verifyNoInteractions(merchantRepository, parkingTransactionRepository);
    }
}