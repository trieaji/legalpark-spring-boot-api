package com.soloproject.LegalPark.services.parkingTransactions.user;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingEntryRequest;
import com.soloproject.LegalPark.dto.request.parkingTransaction.ParkingExitRequest;
import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.dto.response.parkingTransaction.ParkingTransactionResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ParkingTransactionResponseMapper;
import com.soloproject.LegalPark.repository.*;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.parkingTransaction.users.UserParkingTransactionServiceImpl;
import com.soloproject.LegalPark.service.payment.IPaymentService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import com.soloproject.LegalPark.util.PaymentResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserParkingTransactionServiceImplTest {

    @Mock
    private ParkingTransactionRepository parkingTransactionRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private UsersRepository usersRepository; // Meskipun tidak di-autowire, beberapa method mungkin menggunakan user
    @Mock
    private IPaymentService paymentService;
    @Mock
    private ParkingTransactionResponseMapper parkingTransactionResponseMapper;
    @Mock
    private INotificationService iNotificationService;
    @Mock
    private ITemplateService iTemplateService;

    @InjectMocks
    private UserParkingTransactionServiceImpl userParkingTransactionService;

    private MockedStatic<ResponseHandler> mockedResponseHandler;

    @BeforeEach
    void setUp() {
        // Initialize userParkingTransactionService with mocked constructor dependencies
        userParkingTransactionService = new UserParkingTransactionServiceImpl(iNotificationService, iTemplateService);
        // Manually inject other mocks as they are @Autowired
        MockitoAnnotations.openMocks(this); // This line is crucial for @InjectMocks to work correctly after manual instantiation
        mockedResponseHandler = Mockito.mockStatic(ResponseHandler.class);
        // Default mocks for ResponseHandler's static methods
        when(ResponseHandler.generateResponseSuccess(any(), any(), any())).thenAnswer(invocation -> {
            HttpStatus status = invocation.getArgument(0);
            String message = invocation.getArgument(1);
            Object data = invocation.getArgument(2);
            ResponseHandler rh = new ResponseHandler();
            rh.setCode(status.value());
            rh.setStatus(status);
            rh.setMessage(message);
            rh.setData(data);
            return ResponseEntity.status(status).body(rh);
        });
        when(ResponseHandler.generateResponseSuccess(any())).thenAnswer(invocation -> {
            Object data = invocation.getArgument(0);
            ResponseHandler rh = new ResponseHandler();
            rh.setCode(HttpStatus.OK.value());
            rh.setStatus(HttpStatus.OK);
            rh.setMessage("success");
            rh.setData(data);
            return ResponseEntity.status(HttpStatus.OK).body(rh);
        });
        when(ResponseHandler.generateResponseError(any(), any(), any())).thenAnswer(invocation -> {
            HttpStatus status = invocation.getArgument(0);
            Object error = invocation.getArgument(1);
            String message = invocation.getArgument(2);
            ResponseHandler rh = new ResponseHandler();
            rh.setCode(status.value());
            rh.setStatus(status);
            rh.setMessage(message);
            rh.setError(error);
            return ResponseEntity.status(status).body(rh);
        });
    }

    @AfterEach
    void tearDown() {
        mockedResponseHandler.close();
    }

    // Helper method to create a mock User
    private Users createMockUser(String id, String email, String accountName, BigDecimal balance) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        user.setAccountName(accountName);
        user.setBalance(balance);
        user.setRole(Role.USER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword("hashed_password");
        return user;
    }

    // Helper method to create a mock Vehicle
    private Vehicle createMockVehicle(String id, String licensePlate, VehicleType type, Users owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setType(type);
        vehicle.setOwner(owner);
        return vehicle;
    }

    // Helper method to create a mock Merchant
    private Merchant createMockMerchant(String id, String code, String name) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setMerchantCode(code);
        merchant.setMerchantName(name);
        return merchant;
    }

    // Helper method to create a mock ParkingSpot
    private ParkingSpot createMockParkingSpot(String id, String spotNumber, SpotType spotType, ParkingSpotStatus status, Integer floor, Merchant merchant) {
        ParkingSpot spot = new ParkingSpot();
        spot.setId(id);
        spot.setSpotNumber(spotNumber);
        spot.setSpotType(spotType);
        spot.setStatus(status);
        spot.setFloor(floor);
        spot.setMerchant(merchant);
        return spot;
    }

    // Helper method to create a mock ParkingTransaction
    private ParkingTransaction createMockParkingTransaction(String id, Vehicle vehicle, ParkingSpot parkingSpot, ParkingStatus status, PaymentStatus paymentStatus, LocalDateTime entryTime, LocalDateTime exitTime, BigDecimal totalCost) {
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setId(id);
        transaction.setVehicle(vehicle);
        transaction.setParkingSpot(parkingSpot);
        transaction.setStatus(status);
        transaction.setPaymentStatus(paymentStatus);
        transaction.setEntryTime(entryTime);
        transaction.setExitTime(exitTime);
        transaction.setTotalCost(totalCost);
        return transaction;
    }

    // Helper method to create a mock ParkingTransactionResponse
    private ParkingTransactionResponse createMockParkingTransactionResponse(String id, VehicleResponse vehicleResponse, ParkingSpotResponse parkingSpotResponse, String status, String paymentStatus, LocalDateTime entryTime, LocalDateTime exitTime, BigDecimal totalCost) {
        ParkingTransactionResponse response = new ParkingTransactionResponse();
        response.setId(id);
        response.setVehicle(vehicleResponse);
        response.setParkingSpot(parkingSpotResponse);
        response.setStatus(status);
        response.setPaymentStatus(paymentStatus);
        response.setEntryTime(entryTime);
        response.setExitTime(exitTime);
        response.setTotalCost(totalCost);
        return response;
    }

    // Helper for VehicleResponse
    private VehicleResponse createMockVehicleResponse(String id, String licensePlate, String type) {
        VehicleResponse res = new VehicleResponse();
        res.setId(id);
        res.setLicensePlate(licensePlate);
        res.setType(type);
        return res;
    }

    // Helper for ParkingSpotResponse
    private ParkingSpotResponse createMockParkingSpotResponse(String id, String spotNumber, String spotType, String status) {
        ParkingSpotResponse res = new ParkingSpotResponse();
        res.setId(id);
        res.setSpotNumber(spotNumber);
        res.setSpotType(spotType);
        res.setStatus(status);
        return res;
    }


    // ====================================================================================================
    // TEST UNTUK METODE recordParkingEntry
    // ====================================================================================================

    @Test
    void recordParkingEntry_Success_WithSpecificSpot() {
        // 1. Persiapan Data Mock
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String spotNumber = "A01";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", spotNumber, SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingTransaction newTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, LocalDateTime.now(), null, null);

        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setSpotNumber(spotNumber);

        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse parkingSpotResponse = createMockParkingSpotResponse(parkingSpot.getId(), parkingSpot.getSpotNumber(), parkingSpot.getSpotType().name(), ParkingSpotStatus.OCCUPIED.name());
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(newTransaction.getId(), vehicleResponse, parkingSpotResponse, ParkingStatus.ACTIVE.name(), PaymentStatus.PENDING.name(), newTransaction.getEntryTime(), null, null);


        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());
        when(parkingSpotRepository.findBySpotNumberAndMerchant(spotNumber, merchant)).thenReturn(Optional.of(parkingSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot); // Mock save to return the same spot after status update
        when(parkingTransactionRepository.save(any(ParkingTransaction.class))).thenReturn(newTransaction);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(newTransaction)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);

        ParkingTransactionResponse result = (ParkingTransactionResponse) responseHandler.getData();
        assertEquals(newTransaction.getId(), result.getId());
        assertEquals(request.getLicensePlate(), result.getVehicle().getLicensePlate());
        assertEquals(request.getSpotNumber(), result.getParkingSpot().getSpotNumber());
        assertEquals(ParkingStatus.ACTIVE.name(), result.getStatus());
        assertEquals(PaymentStatus.PENDING.name(), result.getPaymentStatus());

        // Verifikasi interaksi
        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(spotNumber, merchant);
        verify(parkingSpotRepository).save(argThat(spot -> spot.getStatus() == ParkingSpotStatus.OCCUPIED));
        verify(parkingTransactionRepository).save(any(ParkingTransaction.class));
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(newTransaction);
    }

    @Test
    void recordParkingEntry_Success_AutoAllocateSpot() {
        // 1. Persiapan Data Mock
        String licensePlate = "B5678JKL";
        String merchantCode = "MERCH002";

        Users user = createMockUser("user2", "user2@example.com", "User Two", new BigDecimal("50000"));
        Vehicle vehicle = createMockVehicle("veh2", licensePlate, VehicleType.MOTORCYCLE, user);
        Merchant merchant = createMockMerchant("merch2", merchantCode, "Office Tower");
        ParkingSpot availableSpot = createMockParkingSpot("spot2", "B05", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 2, merchant);
        ParkingTransaction newTransaction = createMockParkingTransaction("trans2", vehicle, availableSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, LocalDateTime.now(), null, null);

        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setSpotNumber(null); // Auto-allocation

        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse parkingSpotResponse = createMockParkingSpotResponse(availableSpot.getId(), availableSpot.getSpotNumber(), availableSpot.getSpotType().name(), ParkingSpotStatus.OCCUPIED.name());
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(newTransaction.getId(), vehicleResponse, parkingSpotResponse, ParkingStatus.ACTIVE.name(), PaymentStatus.PENDING.name(), newTransaction.getEntryTime(), null, null);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());
        when(parkingSpotRepository.findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE)).thenReturn(Collections.singletonList(availableSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(availableSpot);
        when(parkingTransactionRepository.save(any(ParkingTransaction.class))).thenReturn(newTransaction);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(newTransaction)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);

        ParkingTransactionResponse result = (ParkingTransactionResponse) responseHandler.getData();
        assertEquals(newTransaction.getId(), result.getId());
        assertEquals(request.getLicensePlate(), result.getVehicle().getLicensePlate());
        assertEquals(availableSpot.getSpotNumber(), result.getParkingSpot().getSpotNumber()); // Should be the auto-allocated spot
        assertEquals(ParkingStatus.ACTIVE.name(), result.getStatus());
        assertEquals(PaymentStatus.PENDING.name(), result.getPaymentStatus());

        // Verifikasi interaksi
        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(parkingSpotRepository).findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotRepository, never()).findBySpotNumberAndMerchant(anyString(), any(Merchant.class)); // Should not call specific spot lookup
        verify(parkingSpotRepository).save(argThat(spot -> spot.getStatus() == ParkingSpotStatus.OCCUPIED));
        verify(parkingTransactionRepository).save(any(ParkingTransaction.class));
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(newTransaction);
    }

    @Test
    void recordParkingEntry_VehicleNotFound_ReturnsNotFound() {
        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate("NOTFOUND");
        request.setMerchantCode("MERCH001");

        when(vehicleRepository.findByLicensePlate("NOTFOUND")).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Vehicle with license plate 'NOTFOUND' not registered.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate("NOTFOUND");
        verifyNoInteractions(merchantRepository, parkingTransactionRepository, parkingSpotRepository);
    }

    @Test
    void recordParkingEntry_MerchantNotFound_ReturnsNotFound() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "NOTFOUND";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);

        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Merchant not found with code: " + merchantCode, responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verifyNoInteractions(parkingTransactionRepository, parkingSpotRepository);
    }

    @Test
    void recordParkingEntry_VehicleAlreadyHasActiveSession_ReturnsConflict() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant); // Occupied
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, LocalDateTime.now().minusHours(1), null, null);


        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Vehicle '" + licensePlate + "' already has an active parking session.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verifyNoInteractions(parkingSpotRepository);
    }

    @Test
    void recordParkingEntry_SpecificSpotNotFound_ReturnsNotFound() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String spotNumber = "NOTFOUND";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");

        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setSpotNumber(spotNumber);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());
        when(parkingSpotRepository.findBySpotNumberAndMerchant(spotNumber, merchant)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Parking spot '" + spotNumber + "' not found at merchant '" + merchantCode + "'.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(spotNumber, merchant);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void recordParkingEntry_SpecificSpotNotAvailable_ReturnsConflict() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String spotNumber = "A01";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", spotNumber, SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant); // Not available

        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setSpotNumber(spotNumber);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());
        when(parkingSpotRepository.findBySpotNumberAndMerchant(spotNumber, merchant)).thenReturn(Optional.of(parkingSpot));

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Parking spot '" + spotNumber + "' is not available. Current status: " + ParkingSpotStatus.OCCUPIED.name(), responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(spotNumber, merchant);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void recordParkingEntry_NoAvailableSpotsForAutoAllocation_ReturnsServiceUnavailable() {
        String licensePlate = "B5678JKL";
        String merchantCode = "MERCH002";

        Users user = createMockUser("user2", "user2@example.com", "User Two", new BigDecimal("50000"));
        Vehicle vehicle = createMockVehicle("veh2", licensePlate, VehicleType.MOTORCYCLE, user);
        Merchant merchant = createMockMerchant("merch2", merchantCode, "Office Tower");

        ParkingEntryRequest request = new ParkingEntryRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setSpotNumber(null); // Auto-allocation

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());
        when(parkingSpotRepository.findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingEntry(request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("No available parking spots at merchant '" + merchantCode + "'.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(parkingSpotRepository).findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE recordParkingExit
    // ====================================================================================================

    @Test
    void recordParkingExit_Success_PaymentSuccess() {
        // 1. Persiapan Data Mock
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String verificationCode = "123456";
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(75); // 1 jam 15 menit
        BigDecimal expectedCost = new BigDecimal("10000.00"); // Should be 2 hours * 5000 = 10000

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("20000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, entryTime, null, null);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode(verificationCode);

        // Prepare expected updated transaction and response
        ParkingTransaction completedTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.COMPLETED, PaymentStatus.PAID, entryTime, LocalDateTime.now(), expectedCost);
        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse parkingSpotResponse = createMockParkingSpotResponse(parkingSpot.getId(), parkingSpot.getSpotNumber(), parkingSpot.getSpotType().name(), ParkingSpotStatus.AVAILABLE.name());
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(completedTransaction.getId(), vehicleResponse, parkingSpotResponse, ParkingStatus.COMPLETED.name(), PaymentStatus.PAID.name(), completedTransaction.getEntryTime(), completedTransaction.getExitTime(), completedTransaction.getTotalCost());

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));
        when(paymentService.processParkingPayment(eq(user.getId()), any(BigDecimal.class), eq(activeTransaction.getId()), eq(verificationCode)))
                .thenReturn(PaymentResult.SUCCESS);
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot);
        when(parkingTransactionRepository.save(any(ParkingTransaction.class))).thenReturn(completedTransaction);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(completedTransaction)).thenReturn(expectedResponse);

        // Mock email notification and template service
        String emailBody = "<html>Email Body</html>";
        when(iTemplateService.processEmailTemplate(eq("payment_success_confirmation"), any(Map.class))).thenReturn(emailBody);
        when(iNotificationService.sendEmailNotification(any(EmailNotificationRequest.class))).thenReturn(ResponseEntity.ok("Email sent"));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);

        ParkingTransactionResponse result = (ParkingTransactionResponse) responseHandler.getData();
        assertEquals(completedTransaction.getId(), result.getId());
        assertEquals(PaymentStatus.PAID.name(), result.getPaymentStatus());
        assertEquals(ParkingStatus.COMPLETED.name(), result.getStatus());
        assertEquals(expectedCost, result.getTotalCost());
        assertNotNull(result.getExitTime()); // Exit time should be set

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(paymentService).processParkingPayment(eq(user.getId()), argThat(cost -> cost.compareTo(expectedCost) == 0), eq(activeTransaction.getId()), eq(verificationCode));
        verify(parkingSpotRepository).save(argThat(spot -> spot.getStatus() == ParkingSpotStatus.AVAILABLE));
        verify(parkingTransactionRepository).save(argThat(trans -> trans.getStatus() == ParkingStatus.COMPLETED && trans.getPaymentStatus() == PaymentStatus.PAID && trans.getTotalCost().compareTo(expectedCost) == 0));
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(completedTransaction);

        // Verify email sending
        verify(iTemplateService).processEmailTemplate(eq("payment_success_confirmation"), any(Map.class));
        verify(iNotificationService).sendEmailNotification(argThat(emailReq ->
                emailReq.getTo().equals(user.getEmail()) &&
                        emailReq.getSubject().equals("LegalPark - Konfirmasi Pembayaran Parkir Berhasil!") &&
                        emailReq.getBody().equals(emailBody)
        ));
    }

    @Test
    void recordParkingExit_VehicleNotFound_ReturnsNotFound() {
        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate("NOTFOUND");
        request.setMerchantCode("MERCH001");
        request.setVerificationCode("123456");

        when(vehicleRepository.findByLicensePlate("NOTFOUND")).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Vehicle with license plate 'NOTFOUND' not registered.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate("NOTFOUND");
        verifyNoInteractions(merchantRepository, parkingTransactionRepository, paymentService, parkingSpotRepository);
    }

    @Test
    void recordParkingExit_MerchantNotFound_ReturnsNotFound() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "NOTFOUND";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode("123456");

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Merchant not found with code: " + merchantCode, responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verifyNoInteractions(parkingTransactionRepository, paymentService, parkingSpotRepository);
    }

    @Test
    void recordParkingExit_NoActiveSessionFound_ReturnsNotFound() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode("123456");

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("No active parking session found for vehicle '" + licensePlate + "'.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verifyNoInteractions(paymentService, parkingSpotRepository);
    }

    @Test
    void recordParkingExit_ActiveSessionAtDifferentMerchant_ReturnsBadRequest() {
        String licensePlate = "B1234XYZ";
        String requestedMerchantCode = "MERCH001";
        String actualMerchantCode = "MERCH002"; // Active session is at a different merchant

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant requestedMerchant = createMockMerchant("merch1", requestedMerchantCode, "Mall ABC");
        Merchant actualMerchant = createMockMerchant("merch2", actualMerchantCode, "Office Tower");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, actualMerchant); // Spot belongs to actualMerchant
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, LocalDateTime.now().minusHours(1), null, null);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(requestedMerchantCode); // Requesting exit from different merchant
        request.setVerificationCode("123456");

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(requestedMerchantCode)).thenReturn(Optional.of(requestedMerchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));

        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Active parking session for this vehicle is not at the specified merchant.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(requestedMerchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verifyNoInteractions(paymentService, parkingSpotRepository);
    }

    @Test
    void recordParkingExit_PaymentFailed_InsufficientBalance_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String verificationCode = "123456";
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(30);
        BigDecimal expectedCost = new BigDecimal("5000.00");

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("1000")); // Low balance
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, entryTime, null, null);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode(verificationCode);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));
        when(paymentService.processParkingPayment(eq(user.getId()), any(BigDecimal.class), eq(activeTransaction.getId()), eq(verificationCode)))
                .thenReturn(PaymentResult.INSUFFICIENT_BALANCE);
        when(parkingTransactionRepository.save(any(ParkingTransaction.class))).thenReturn(activeTransaction); // Mock saving of failed payment status

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertTrue(responseHandler.getMessage().contains("Insufficient balance"));

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(paymentService).processParkingPayment(eq(user.getId()), any(BigDecimal.class), eq(activeTransaction.getId()), eq(verificationCode));
        verify(parkingTransactionRepository).save(argThat(trans -> trans.getPaymentStatus() == PaymentStatus.FAILED)); // Verify status updated to FAILED
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class)); // Spot status should not change
        verifyNoInteractions(iNotificationService, iTemplateService); // No email on failure
    }

    @Test
    void recordParkingExit_PaymentFailed_OtherReason_ReturnsInternalServerError() {
        // 1. Persiapan Data Mock
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String verificationCode = "INVALID_CODE"; // Simulate invalid code causing FAILED_OTHER
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(30);
        BigDecimal expectedCost = new BigDecimal("5000.00");

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("20000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, entryTime, null, null);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode(verificationCode);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));
        when(paymentService.processParkingPayment(eq(user.getId()), any(BigDecimal.class), eq(activeTransaction.getId()), eq(verificationCode)))
                .thenReturn(PaymentResult.FAILED_OTHER); // Payment fails for other reason
        when(parkingTransactionRepository.save(any(ParkingTransaction.class))).thenReturn(activeTransaction); // Mock saving of failed payment status

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Payment failed. Please check your verification code and try again.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(paymentService).processParkingPayment(eq(user.getId()), any(BigDecimal.class), eq(activeTransaction.getId()), eq(verificationCode));
        verify(parkingTransactionRepository).save(argThat(trans -> trans.getPaymentStatus() == PaymentStatus.FAILED)); // Verify status updated to FAILED
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class)); // Spot status should not change
        verifyNoInteractions(iNotificationService, iTemplateService); // No email on failure
    }

    @Test
    void recordParkingExit_VehicleOwnerInfoMissing_ThrowsRuntimeException() {
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String verificationCode = "123456";
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(30);

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("20000"));
        // Simulate missing owner info by setting owner to null or owner.id to null
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, null); // Owner is null
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, entryTime, null, null);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode(verificationCode);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));

        // Expect RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userParkingTransactionService.recordParkingExit(request);
        });

        assertEquals("Vehicle owner information missing for payment. Cannot process payment.", thrown.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verifyNoInteractions(paymentService, parkingSpotRepository, parkingTransactionResponseMapper, iNotificationService, iTemplateService);
    }

    @Test
    void recordParkingExit_EmailSendingFails_DoesNotRollbackTransaction() {
        // 1. Persiapan Data Mock
        String licensePlate = "B1234XYZ";
        String merchantCode = "MERCH001";
        String verificationCode = "123456";
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(75); // 1 hour 15 minutes
        BigDecimal expectedCost = new BigDecimal("10000.00"); // Should be 2 hours * 5000 = 10000

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("20000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", merchantCode, "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, entryTime, null, null);

        ParkingExitRequest request = new ParkingExitRequest();
        request.setLicensePlate(licensePlate);
        request.setMerchantCode(merchantCode);
        request.setVerificationCode(verificationCode);

        // Prepare expected updated transaction and response
        ParkingTransaction completedTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.COMPLETED, PaymentStatus.PAID, entryTime, LocalDateTime.now(), expectedCost);
        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse parkingSpotResponse = createMockParkingSpotResponse(parkingSpot.getId(), parkingSpot.getSpotNumber(), parkingSpot.getSpotType().name(), ParkingSpotStatus.AVAILABLE.name());
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(completedTransaction.getId(), vehicleResponse, parkingSpotResponse, ParkingStatus.COMPLETED.name(), PaymentStatus.PAID.name(), completedTransaction.getEntryTime(), completedTransaction.getExitTime(), completedTransaction.getTotalCost());

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));
        when(paymentService.processParkingPayment(eq(user.getId()), any(BigDecimal.class), eq(activeTransaction.getId()), eq(verificationCode)))
                .thenReturn(PaymentResult.SUCCESS);
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot);
        when(parkingTransactionRepository.save(any(ParkingTransaction.class))).thenReturn(completedTransaction);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(completedTransaction)).thenReturn(expectedResponse);

        // Mock email notification and template service to THROW EXCEPTION
        String emailBody = "<html>Email Body</html>";
        when(iTemplateService.processEmailTemplate(eq("payment_success_confirmation"), any(Map.class))).thenReturn(emailBody);
        when(iNotificationService.sendEmailNotification(any(EmailNotificationRequest.class))).thenThrow(new RuntimeException("Email service down!"));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingTransactionService.recordParkingExit(request);

        // 4. Verifikasi Hasil - Should still be OK because email failure doesn't block core transaction
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);

        ParkingTransactionResponse result = (ParkingTransactionResponse) responseHandler.getData();
        assertEquals(completedTransaction.getId(), result.getId());
        assertEquals(PaymentStatus.PAID.name(), result.getPaymentStatus());
        assertEquals(ParkingStatus.COMPLETED.name(), result.getStatus());
        assertEquals(expectedCost, result.getTotalCost());
        assertNotNull(result.getExitTime());

        verify(paymentService).processParkingPayment(eq(user.getId()), argThat(cost -> cost.compareTo(expectedCost) == 0), eq(activeTransaction.getId()), eq(verificationCode));
        verify(parkingSpotRepository).save(argThat(spot -> spot.getStatus() == ParkingSpotStatus.AVAILABLE));
        verify(parkingTransactionRepository).save(argThat(trans -> trans.getStatus() == ParkingStatus.COMPLETED && trans.getPaymentStatus() == PaymentStatus.PAID));

        // Verify email sending was attempted and failed
        verify(iTemplateService).processEmailTemplate(eq("payment_success_confirmation"), any(Map.class));
        verify(iNotificationService).sendEmailNotification(any(EmailNotificationRequest.class));
    }


    // ====================================================================================================
    // TEST UNTUK METODE calculateParkingCost (private method, diuji melalui recordParkingExit)
    // ====================================================================================================

    @Test
    void calculateParkingCost_DurationLessThanOneHour_ChargesOneHour() {
        LocalDateTime entry = LocalDateTime.now().minusMinutes(30);
        LocalDateTime exit = LocalDateTime.now();
        ParkingTransaction transaction = createMockParkingTransaction("id", null, null, null, null, entry, exit, null);

        // Use reflection to call the private method, or simply confirm it's called correctly in recordParkingExit tests
        // For demonstration purposes, let's call it via reflection for direct testing.
        try {
            java.lang.reflect.Method method = UserParkingTransactionServiceImpl.class.getDeclaredMethod("calculateParkingCost", ParkingTransaction.class);
            method.setAccessible(true);
            BigDecimal cost = (BigDecimal) method.invoke(userParkingTransactionService, transaction);
            assertEquals(new BigDecimal("5000"), cost); // 1 hour rate
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }

    @Test
    void calculateParkingCost_DurationExactlyOneHour_ChargesOneHour() {
        LocalDateTime entry = LocalDateTime.now().minusHours(1);
        LocalDateTime exit = LocalDateTime.now();
        ParkingTransaction transaction = createMockParkingTransaction("id", null, null, null, null, entry, exit, null);

        try {
            java.lang.reflect.Method method = UserParkingTransactionServiceImpl.class.getDeclaredMethod("calculateParkingCost", ParkingTransaction.class);
            method.setAccessible(true);
            BigDecimal cost = (BigDecimal) method.invoke(userParkingTransactionService, transaction);
            assertEquals(new BigDecimal("5000"), cost); // 1 hour rate
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }

    @Test
    void calculateParkingCost_DurationMoreThanOneHourLessThanTwoHours_ChargesTwoHours() {
        LocalDateTime entry = LocalDateTime.now().minusMinutes(61); // 1 hour 1 minute
        LocalDateTime exit = LocalDateTime.now();
        ParkingTransaction transaction = createMockParkingTransaction("id", null, null, null, null, entry, exit, null);

        try {
            java.lang.reflect.Method method = UserParkingTransactionServiceImpl.class.getDeclaredMethod("calculateParkingCost", ParkingTransaction.class);
            method.setAccessible(true);
            BigDecimal cost = (BigDecimal) method.invoke(userParkingTransactionService, transaction);
            assertEquals(new BigDecimal("10000"), cost); // 2 hour rate
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }

    @Test
    void calculateParkingCost_DurationExactlyTwoHours_ChargesTwoHours() {
        LocalDateTime entry = LocalDateTime.now().minusHours(2);
        LocalDateTime exit = LocalDateTime.now();
        ParkingTransaction transaction = createMockParkingTransaction("id", null, null, null, null, entry, exit, null);

        try {
            java.lang.reflect.Method method = UserParkingTransactionServiceImpl.class.getDeclaredMethod("calculateParkingCost", ParkingTransaction.class);
            method.setAccessible(true);
            BigDecimal cost = (BigDecimal) method.invoke(userParkingTransactionService, transaction);
            assertEquals(new BigDecimal("10000"), cost); // 2 hour rate
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }

    @Test
    void calculateParkingCost_DurationIsZero_ChargesZero() {
        LocalDateTime now = LocalDateTime.now();
        ParkingTransaction transaction = createMockParkingTransaction("id", null, null, null, null, now, now, null);

        try {
            java.lang.reflect.Method method = UserParkingTransactionServiceImpl.class.getDeclaredMethod("calculateParkingCost", ParkingTransaction.class);
            method.setAccessible(true);
            BigDecimal cost = (BigDecimal) method.invoke(userParkingTransactionService, transaction);
            assertEquals(BigDecimal.ZERO, cost);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }


    // ====================================================================================================
    // TEST UNTUK METODE getUserActiveParkingTransaction
    // ====================================================================================================

    @Test
    void getUserActiveParkingTransaction_Success_ActiveTransactionFound() {
        String licensePlate = "B1234XYZ";
        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", "MERCH001", "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);
        ParkingTransaction activeTransaction = createMockParkingTransaction("trans1", vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING, LocalDateTime.now().minusHours(1), null, null);

        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse parkingSpotResponse = createMockParkingSpotResponse(parkingSpot.getId(), parkingSpot.getSpotNumber(), parkingSpot.getSpotType().name(), ParkingSpotStatus.OCCUPIED.name());
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(activeTransaction.getId(), vehicleResponse, parkingSpotResponse, ParkingStatus.ACTIVE.name(), PaymentStatus.PENDING.name(), activeTransaction.getEntryTime(), null, null);


        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.of(activeTransaction));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(activeTransaction)).thenReturn(expectedResponse);

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserActiveParkingTransaction(licensePlate);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);

        ParkingTransactionResponse result = (ParkingTransactionResponse) responseHandler.getData();
        assertEquals(expectedResponse.getId(), result.getId());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(activeTransaction);
    }

    @Test
    void getUserActiveParkingTransaction_VehicleNotFound_ReturnsNotFound() {
        String licensePlate = "NOTFOUND";

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserActiveParkingTransaction(licensePlate);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Vehicle with license plate '" + licensePlate + "' not registered.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verifyNoInteractions(parkingTransactionRepository, parkingTransactionResponseMapper);
    }

    @Test
    void getUserActiveParkingTransaction_NoActiveTransaction_ReturnsNotFoundWithMessage() {
        String licensePlate = "B1234XYZ";
        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(parkingTransactionRepository.findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserActiveParkingTransaction(licensePlate);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()); // Note: The service returns NOT_FOUND with "SUCCESS" message for no active session
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("SUCCESS", responseHandler.getError()); // Original code sets error to "SUCCESS"
        assertEquals("No active parking session found for vehicle '" + licensePlate + "'.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(parkingTransactionRepository).findByVehicleAndStatus(vehicle, ParkingStatus.ACTIVE);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // ====================================================================================================
    // TEST UNTUK METODE getUserParkingTransactionHistory
    // ====================================================================================================

    @Test
    void getUserParkingTransactionHistory_Success_ReturnsListOfTransactions() {
        String licensePlate = "B1234XYZ";
        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", "MERCH001", "Mall ABC");
        ParkingSpot spot1 = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot spot2 = createMockParkingSpot("spot2", "B01", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 1, merchant);

        ParkingTransaction trans1 = createMockParkingTransaction("trans1", vehicle, spot1, ParkingStatus.COMPLETED, PaymentStatus.PAID, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2).plusHours(2), new BigDecimal("10000"));
        ParkingTransaction trans2 = createMockParkingTransaction("trans2", vehicle, spot2, ParkingStatus.COMPLETED, PaymentStatus.PAID, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1), new BigDecimal("5000"));

        List<ParkingTransaction> transactions = Arrays.asList(trans1, trans2);

        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), spot1.getSpotNumber(), spot1.getSpotType().name(), spot1.getStatus().name());
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), spot2.getSpotNumber(), spot2.getSpotType().name(), spot2.getStatus().name());

        ParkingTransactionResponse res1 = createMockParkingTransactionResponse(trans1.getId(), vehicleResponse, spotResp1, trans1.getStatus().name(), trans1.getPaymentStatus().name(), trans1.getEntryTime(), trans1.getExitTime(), trans1.getTotalCost());
        ParkingTransactionResponse res2 = createMockParkingTransactionResponse(trans2.getId(), vehicleResponse, spotResp2, trans2.getStatus().name(), trans2.getPaymentStatus().name(), trans2.getEntryTime(), trans2.getExitTime(), trans2.getTotalCost());

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(parkingTransactionRepository.findByVehicle(vehicle)).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(res1);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans2)).thenReturn(res2);

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserParkingTransactionHistory(licensePlate);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<?> dataList = (List<?>) responseHandler.getData();
        assertEquals(2, dataList.size());
        assertTrue(dataList.get(0) instanceof ParkingTransactionResponse);

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(parkingTransactionRepository).findByVehicle(vehicle);
        verify(parkingTransactionResponseMapper, times(2)).mapToParkingTransactionResponse(any(ParkingTransaction.class));
    }

    @Test
    void getUserParkingTransactionHistory_VehicleNotFound_ReturnsNotFound() {
        String licensePlate = "NOTFOUND";

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserParkingTransactionHistory(licensePlate);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Vehicle with license plate '" + licensePlate + "' not registered.", responseHandler.getMessage());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verifyNoInteractions(parkingTransactionRepository, parkingTransactionResponseMapper);
    }

    @Test
    void getUserParkingTransactionHistory_NoTransactionsFound_ReturnsEmptyList() {
        String licensePlate = "B1234XYZ";
        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicle));
        when(parkingTransactionRepository.findByVehicle(vehicle)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserParkingTransactionHistory(licensePlate);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<?> dataList = (List<?>) responseHandler.getData();
        assertTrue(dataList.isEmpty());

        verify(vehicleRepository).findByLicensePlate(licensePlate);
        verify(parkingTransactionRepository).findByVehicle(vehicle);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // ====================================================================================================
    // TEST UNTUK METODE getUserParkingTransactionDetails
    // ====================================================================================================

    @Test
    void getUserParkingTransactionDetails_Success_TransactionFoundAndBelongsToVehicle() {
        String transactionId = "trans1";
        String licensePlate = "B1234XYZ";
        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle vehicle = createMockVehicle("veh1", licensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", "MERCH001", "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingTransaction transaction = createMockParkingTransaction(transactionId, vehicle, parkingSpot, ParkingStatus.COMPLETED, PaymentStatus.PAID, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), new BigDecimal("5000"));

        VehicleResponse vehicleResponse = createMockVehicleResponse(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getType().name());
        ParkingSpotResponse parkingSpotResponse = createMockParkingSpotResponse(parkingSpot.getId(), parkingSpot.getSpotNumber(), parkingSpot.getSpotType().name(), parkingSpot.getStatus().name());
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(transactionId, vehicleResponse, parkingSpotResponse, transaction.getStatus().name(), transaction.getPaymentStatus().name(), transaction.getEntryTime(), transaction.getExitTime(), transaction.getTotalCost());


        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(transaction)).thenReturn(expectedResponse);

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserParkingTransactionDetails(transactionId, licensePlate);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);

        ParkingTransactionResponse result = (ParkingTransactionResponse) responseHandler.getData();
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getVehicle().getLicensePlate(), result.getVehicle().getLicensePlate());

        verify(parkingTransactionRepository).findById(transactionId);
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(transaction);
    }

    @Test
    void getUserParkingTransactionDetails_TransactionNotFound_ReturnsNotFound() {
        String transactionId = "NOTFOUND";
        String licensePlate = "B1234XYZ";

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserParkingTransactionDetails(transactionId, licensePlate);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Parking transaction not found with ID: " + transactionId, responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    @Test
    void getUserParkingTransactionDetails_TransactionDoesNotBelongToVehicle_ReturnsForbidden() {
        String transactionId = "trans1";
        String correctLicensePlate = "B1234XYZ";
        String wrongLicensePlate = "X9999ABC";

        Users user = createMockUser("user1", "user@example.com", "User One", new BigDecimal("100000"));
        Vehicle correctVehicle = createMockVehicle("veh1", correctLicensePlate, VehicleType.CAR, user);
        Merchant merchant = createMockMerchant("merch1", "MERCH001", "Mall ABC");
        ParkingSpot parkingSpot = createMockParkingSpot("spot1", "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingTransaction transaction = createMockParkingTransaction(transactionId, correctVehicle, parkingSpot, ParkingStatus.COMPLETED, PaymentStatus.PAID, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), new BigDecimal("5000"));


        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        ResponseEntity<Object> responseEntity = userParkingTransactionService.getUserParkingTransactionDetails(transactionId, wrongLicensePlate);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Access denied. This transaction does not belong to the specified vehicle.", responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }
}