package com.soloproject.LegalPark.services.parkingTransactions.admin;

import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.dto.response.parkingTransaction.ParkingTransactionResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ParkingTransactionResponseMapper;
import com.soloproject.LegalPark.repository.*;
import com.soloproject.LegalPark.service.parkingTransaction.admin.AdminParkingTransactionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminParkingTransactionServiceImplTest {

    @Mock
    private ParkingTransactionRepository parkingTransactionRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private ParkingTransactionResponseMapper parkingTransactionResponseMapper;

    @InjectMocks
    private AdminParkingTransactionServiceImpl adminParkingTransactionService;

    private MockedStatic<ResponseHandler> mockedResponseHandler;

    @BeforeEach
    void setUp() {
        mockedResponseHandler = Mockito.mockStatic(ResponseHandler.class);
        // Default mocks for ResponseHandler's static methods
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

    // Helper method to create a mock User (owner of vehicle)
    private Users createMockUser(String id) {
        Users user = new Users();
        user.setId(id);
        user.setEmail("user" + id + "@example.com");
        user.setAccountName("User " + id);
        return user;
    }

    // Helper method to create a mock Vehicle
    private Vehicle createMockVehicle(String id, String licensePlate, Users owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setType(VehicleType.CAR); // Default type
        vehicle.setOwner(owner);
        return vehicle;
    }

    // Helper method to create a mock Merchant
    private Merchant createMockMerchant(String id, String code) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setMerchantCode(code);
        merchant.setMerchantName("Merchant " + code);
        return merchant;
    }

    // Helper method to create a mock ParkingSpot
    private ParkingSpot createMockParkingSpot(String id, String spotNumber, Merchant merchant, ParkingSpotStatus status) {
        ParkingSpot spot = new ParkingSpot();
        spot.setId(id);
        spot.setSpotNumber(spotNumber);
        spot.setSpotType(SpotType.CAR); // Default type
        spot.setMerchant(merchant);
        spot.setStatus(status);
        return spot;
    }

    // Helper method to create a mock ParkingTransaction
    private ParkingTransaction createMockParkingTransaction(String id, Vehicle vehicle, ParkingSpot parkingSpot, ParkingStatus status, PaymentStatus paymentStatus) {
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setId(id);
        transaction.setVehicle(vehicle);
        transaction.setParkingSpot(parkingSpot);
        transaction.setStatus(status);
        transaction.setPaymentStatus(paymentStatus);
        transaction.setEntryTime(LocalDateTime.now().minusHours(1)); // Default entry time
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setTotalCost(BigDecimal.ZERO); // Default
        return transaction;
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

    // Helper method to create a mock ParkingTransactionResponse
    private ParkingTransactionResponse createMockParkingTransactionResponse(ParkingTransaction transaction) {
        ParkingTransactionResponse response = new ParkingTransactionResponse();
        response.setId(transaction.getId());
        response.setVehicle(createMockVehicleResponse(transaction.getVehicle().getId(), transaction.getVehicle().getLicensePlate(), transaction.getVehicle().getType().name()));
        if (transaction.getParkingSpot() != null) {
            response.setParkingSpot(createMockParkingSpotResponse(transaction.getParkingSpot().getId(), transaction.getParkingSpot().getSpotNumber(), transaction.getParkingSpot().getSpotType().name(), transaction.getParkingSpot().getStatus().name()));
        }
        response.setStatus(transaction.getStatus().name());
        response.setPaymentStatus(transaction.getPaymentStatus().name());
        response.setEntryTime(transaction.getEntryTime());
        response.setExitTime(transaction.getExitTime());
        response.setTotalCost(transaction.getTotalCost());
        return response;
    }


    // --- Test cases for adminGetAllParkingTransactions ---
    @Test
    void adminGetAllParkingTransactions_Success_ReturnsListOfTransactions() {
        Users user = createMockUser("u1");
        Vehicle vehicle1 = createMockVehicle("v1", "ABC1234", user);
        Vehicle vehicle2 = createMockVehicle("v2", "DEF5678", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot1 = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);
        ParkingSpot spot2 = createMockParkingSpot("ps2", "B02", merchant, ParkingSpotStatus.AVAILABLE);

        ParkingTransaction trans1 = createMockParkingTransaction("t1", vehicle1, spot1, ParkingStatus.ACTIVE, PaymentStatus.PENDING);
        ParkingTransaction trans2 = createMockParkingTransaction("t2", vehicle2, spot2, ParkingStatus.COMPLETED, PaymentStatus.PAID);

        List<ParkingTransaction> transactions = Arrays.asList(trans1, trans2);
        List<ParkingTransactionResponse> expectedResponses = Arrays.asList(createMockParkingTransactionResponse(trans1), createMockParkingTransactionResponse(trans2));

        when(parkingTransactionRepository.findAll()).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(expectedResponses.get(0));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans2)).thenReturn(expectedResponses.get(1));

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetAllParkingTransactions();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertEquals(2, actualResponses.size());
        assertEquals(expectedResponses.get(0).getId(), actualResponses.get(0).getId());
        assertEquals(expectedResponses.get(1).getId(), actualResponses.get(1).getId());

        verify(parkingTransactionRepository).findAll();
        verify(parkingTransactionResponseMapper, times(2)).mapToParkingTransactionResponse(any(ParkingTransaction.class));
    }

    @Test
    void adminGetAllParkingTransactions_Success_ReturnsEmptyListIfNoTransactions() {
        when(parkingTransactionRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetAllParkingTransactions();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertTrue(actualResponses.isEmpty());

        verify(parkingTransactionRepository).findAll();
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // --- Test cases for adminGetParkingTransactionById ---
    @Test
    void adminGetParkingTransactionById_Success_TransactionFound() {
        String transactionId = "t1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);
        ParkingTransaction transaction = createMockParkingTransaction(transactionId, vehicle, spot, ParkingStatus.ACTIVE, PaymentStatus.PENDING);
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(transaction);

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(transaction)).thenReturn(expectedResponse);

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionById(transactionId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);
        assertEquals(expectedResponse.getId(), ((ParkingTransactionResponse) responseHandler.getData()).getId());

        verify(parkingTransactionRepository).findById(transactionId);
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(transaction);
    }

    @Test
    void adminGetParkingTransactionById_NotFound_TransactionNotFound() {
        String transactionId = "nonExistentId";

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionById(transactionId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking transaction not found with ID: " + transactionId, responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // --- Test cases for adminGetParkingTransactionsByVehicleId ---
    @Test
    void adminGetParkingTransactionsByVehicleId_Success_VehicleAndTransactionsFound() {
        String vehicleId = "v1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle(vehicleId, "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot1 = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);
        ParkingSpot spot2 = createMockParkingSpot("ps2", "B02", merchant, ParkingSpotStatus.AVAILABLE);

        ParkingTransaction trans1 = createMockParkingTransaction("t1", vehicle, spot1, ParkingStatus.ACTIVE, PaymentStatus.PENDING);
        ParkingTransaction trans2 = createMockParkingTransaction("t2", vehicle, spot2, ParkingStatus.COMPLETED, PaymentStatus.PAID);

        List<ParkingTransaction> transactions = Arrays.asList(trans1, trans2);
        List<ParkingTransactionResponse> expectedResponses = Arrays.asList(createMockParkingTransactionResponse(trans1), createMockParkingTransactionResponse(trans2));

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(parkingTransactionRepository.findByVehicle(vehicle)).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(expectedResponses.get(0));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans2)).thenReturn(expectedResponses.get(1));

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByVehicleId(vehicleId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertEquals(2, actualResponses.size());

        verify(vehicleRepository).findById(vehicleId);
        verify(parkingTransactionRepository).findByVehicle(vehicle);
        verify(parkingTransactionResponseMapper, times(2)).mapToParkingTransactionResponse(any(ParkingTransaction.class));
    }

    @Test
    void adminGetParkingTransactionsByVehicleId_NotFound_VehicleNotFound() {
        String vehicleId = "nonExistentVeh";

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByVehicleId(vehicleId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Vehicle not found with ID: " + vehicleId, responseHandler.getMessage());

        verify(vehicleRepository).findById(vehicleId);
        verifyNoInteractions(parkingTransactionRepository, parkingTransactionResponseMapper);
    }

    @Test
    void adminGetParkingTransactionsByVehicleId_Success_VehicleFoundButNoTransactions() {
        String vehicleId = "v1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle(vehicleId, "ABC1234", user);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(parkingTransactionRepository.findByVehicle(vehicle)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByVehicleId(vehicleId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertTrue(actualResponses.isEmpty());

        verify(vehicleRepository).findById(vehicleId);
        verify(parkingTransactionRepository).findByVehicle(vehicle);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // --- Test cases for adminGetParkingTransactionsByParkingSpotId ---
    @Test
    void adminGetParkingTransactionsByParkingSpotId_Success_ParkingSpotAndTransactionsFound() {
        String parkingSpotId = "ps1";
        Users user = createMockUser("u1");
        Vehicle vehicle1 = createMockVehicle("v1", "ABC1234", user);
        Vehicle vehicle2 = createMockVehicle("v2", "DEF5678", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot = createMockParkingSpot(parkingSpotId, "A01", merchant, ParkingSpotStatus.OCCUPIED);

        ParkingTransaction trans1 = createMockParkingTransaction("t1", vehicle1, spot, ParkingStatus.ACTIVE, PaymentStatus.PENDING);
        ParkingTransaction trans2 = createMockParkingTransaction("t2", vehicle2, spot, ParkingStatus.COMPLETED, PaymentStatus.PAID);

        List<ParkingTransaction> transactions = Arrays.asList(trans1, trans2);
        List<ParkingTransactionResponse> expectedResponses = Arrays.asList(createMockParkingTransactionResponse(trans1), createMockParkingTransactionResponse(trans2));

        when(parkingSpotRepository.findById(parkingSpotId)).thenReturn(Optional.of(spot));
        when(parkingTransactionRepository.findByParkingSpot(spot)).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(expectedResponses.get(0));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans2)).thenReturn(expectedResponses.get(1));

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByParkingSpotId(parkingSpotId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertEquals(2, actualResponses.size());

        verify(parkingSpotRepository).findById(parkingSpotId);
        verify(parkingTransactionRepository).findByParkingSpot(spot);
        verify(parkingTransactionResponseMapper, times(2)).mapToParkingTransactionResponse(any(ParkingTransaction.class));
    }

    @Test
    void adminGetParkingTransactionsByParkingSpotId_NotFound_ParkingSpotNotFound() {
        String parkingSpotId = "nonExistentSpot";

        when(parkingSpotRepository.findById(parkingSpotId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByParkingSpotId(parkingSpotId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking spot not found with ID: " + parkingSpotId, responseHandler.getMessage());

        verify(parkingSpotRepository).findById(parkingSpotId);
        verifyNoInteractions(parkingTransactionRepository, parkingTransactionResponseMapper);
    }

    @Test
    void adminGetParkingTransactionsByParkingSpotId_Success_ParkingSpotFoundButNoTransactions() {
        String parkingSpotId = "ps1";
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot = createMockParkingSpot(parkingSpotId, "A01", merchant, ParkingSpotStatus.OCCUPIED);

        when(parkingSpotRepository.findById(parkingSpotId)).thenReturn(Optional.of(spot));
        when(parkingTransactionRepository.findByParkingSpot(spot)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByParkingSpotId(parkingSpotId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertTrue(actualResponses.isEmpty());

        verify(parkingSpotRepository).findById(parkingSpotId);
        verify(parkingTransactionRepository).findByParkingSpot(spot);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // --- Test cases for adminGetParkingTransactionsByMerchantId ---
    @Test
    void adminGetParkingTransactionsByMerchantId_Success_MerchantAndTransactionsFound() {
        String merchantId = "m1";
        Users user = createMockUser("u1");
        Vehicle vehicle1 = createMockVehicle("v1", "ABC1234", user);
        Vehicle vehicle2 = createMockVehicle("v2", "DEF5678", user);
        Merchant merchant = createMockMerchant(merchantId, "MALL1");
        ParkingSpot spot1 = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);
        ParkingSpot spot2 = createMockParkingSpot("ps2", "B02", merchant, ParkingSpotStatus.AVAILABLE);

        ParkingTransaction trans1 = createMockParkingTransaction("t1", vehicle1, spot1, ParkingStatus.ACTIVE, PaymentStatus.PENDING);
        ParkingTransaction trans2 = createMockParkingTransaction("t2", vehicle2, spot2, ParkingStatus.COMPLETED, PaymentStatus.PAID);

        List<ParkingTransaction> transactions = Arrays.asList(trans1, trans2);
        List<ParkingTransactionResponse> expectedResponses = Arrays.asList(createMockParkingTransactionResponse(trans1), createMockParkingTransactionResponse(trans2));

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByParkingSpot_Merchant(merchant)).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(expectedResponses.get(0));
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans2)).thenReturn(expectedResponses.get(1));

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByMerchantId(merchantId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertEquals(2, actualResponses.size());

        verify(merchantRepository).findById(merchantId);
        verify(parkingTransactionRepository).findByParkingSpot_Merchant(merchant);
        verify(parkingTransactionResponseMapper, times(2)).mapToParkingTransactionResponse(any(ParkingTransaction.class));
    }

    @Test
    void adminGetParkingTransactionsByMerchantId_NotFound_MerchantNotFound() {
        String merchantId = "nonExistentMerchant";

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByMerchantId(merchantId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant not found with ID: " + merchantId, responseHandler.getMessage());

        verify(merchantRepository).findById(merchantId);
        verifyNoInteractions(parkingTransactionRepository, parkingTransactionResponseMapper);
    }

    @Test
    void adminGetParkingTransactionsByMerchantId_Success_MerchantFoundButNoTransactions() {
        String merchantId = "m1";
        Merchant merchant = createMockMerchant(merchantId, "MALL1");

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(parkingTransactionRepository.findByParkingSpot_Merchant(merchant)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByMerchantId(merchantId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertTrue(actualResponses.isEmpty());

        verify(merchantRepository).findById(merchantId);
        verify(parkingTransactionRepository).findByParkingSpot_Merchant(merchant);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }


    // --- Test cases for adminGetParkingTransactionsByParkingStatus ---
    @Test
    void adminGetParkingTransactionsByParkingStatus_Success_ReturnsListOfTransactions() {
        ParkingStatus status = ParkingStatus.ACTIVE;
        Users user = createMockUser("u1");
        Vehicle vehicle1 = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot1 = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);

        ParkingTransaction trans1 = createMockParkingTransaction("t1", vehicle1, spot1, status, PaymentStatus.PENDING);
        List<ParkingTransaction> transactions = Collections.singletonList(trans1);
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(trans1);

        when(parkingTransactionRepository.findByStatus(status)).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(expectedResponse);

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByParkingStatus(status);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertEquals(1, actualResponses.size());
        assertEquals(expectedResponse.getId(), actualResponses.get(0).getId());

        verify(parkingTransactionRepository).findByStatus(status);
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(trans1);
    }

    @Test
    void adminGetParkingTransactionsByParkingStatus_Success_ReturnsEmptyListIfNoTransactions() {
        ParkingStatus status = ParkingStatus.CANCELLED;

        when(parkingTransactionRepository.findByStatus(status)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByParkingStatus(status);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertTrue(actualResponses.isEmpty());

        verify(parkingTransactionRepository).findByStatus(status);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // --- Test cases for adminGetParkingTransactionsByPaymentStatus ---
    @Test
    void adminGetParkingTransactionsByPaymentStatus_Success_ReturnsListOfTransactions() {
        PaymentStatus paymentStatus = PaymentStatus.PAID;
        Users user = createMockUser("u1");
        Vehicle vehicle1 = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot1 = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);

        ParkingTransaction trans1 = createMockParkingTransaction("t1", vehicle1, spot1, ParkingStatus.COMPLETED, paymentStatus);
        List<ParkingTransaction> transactions = Collections.singletonList(trans1);
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(trans1);

        when(parkingTransactionRepository.findByPaymentStatus(paymentStatus)).thenReturn(transactions);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(trans1)).thenReturn(expectedResponse);

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByPaymentStatus(paymentStatus);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertEquals(1, actualResponses.size());
        assertEquals(expectedResponse.getId(), actualResponses.get(0).getId());

        verify(parkingTransactionRepository).findByPaymentStatus(paymentStatus);
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(trans1);
    }

    @Test
    void adminGetParkingTransactionsByPaymentStatus_Success_ReturnsEmptyListIfNoTransactions() {
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        when(parkingTransactionRepository.findByPaymentStatus(paymentStatus)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminGetParkingTransactionsByPaymentStatus(paymentStatus);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingTransactionResponse> actualResponses = (List<ParkingTransactionResponse>) responseHandler.getData();
        assertTrue(actualResponses.isEmpty());

        verify(parkingTransactionRepository).findByPaymentStatus(paymentStatus);
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    // --- Test cases for adminUpdateParkingTransactionPaymentStatus ---
    @Test
    void adminUpdateParkingTransactionPaymentStatus_Success_UpdatesStatus() {
        String transactionId = "t1";
        PaymentStatus newPaymentStatus = PaymentStatus.PAID;
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot spot = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);
        ParkingTransaction existingTransaction = createMockParkingTransaction(transactionId, vehicle, spot, ParkingStatus.ACTIVE, PaymentStatus.PENDING);
        ParkingTransaction updatedTransaction = createMockParkingTransaction(transactionId, vehicle, spot, ParkingStatus.ACTIVE, newPaymentStatus); // Simulate updated object
        ParkingTransactionResponse expectedResponse = createMockParkingTransactionResponse(updatedTransaction);

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));
        when(parkingTransactionRepository.save(argThat(trans -> trans.getPaymentStatus() == newPaymentStatus))).thenReturn(updatedTransaction);
        when(parkingTransactionResponseMapper.mapToParkingTransactionResponse(updatedTransaction)).thenReturn(expectedResponse);

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminUpdateParkingTransactionPaymentStatus(transactionId, newPaymentStatus);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof ParkingTransactionResponse);
        assertEquals(newPaymentStatus.name(), ((ParkingTransactionResponse) responseHandler.getData()).getPaymentStatus());

        verify(parkingTransactionRepository).findById(transactionId);
        verify(parkingTransactionRepository).save(existingTransaction);
        verify(parkingTransactionResponseMapper).mapToParkingTransactionResponse(updatedTransaction);
    }

    @Test
    void adminUpdateParkingTransactionPaymentStatus_NotFound_TransactionNotFound() {
        String transactionId = "nonExistentId";
        PaymentStatus newPaymentStatus = PaymentStatus.PAID;

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminUpdateParkingTransactionPaymentStatus(transactionId, newPaymentStatus);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking transaction not found with ID: " + transactionId, responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoMoreInteractions(parkingTransactionRepository, parkingTransactionResponseMapper); // save should not be called
    }

    // --- Test cases for adminCancelParkingTransaction ---
    @Test
    void adminCancelParkingTransaction_Success_ActiveTransactionCancelledAndSpotFreed() {
        String transactionId = "t1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot parkingSpot = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.OCCUPIED);
        ParkingTransaction activeTransaction = createMockParkingTransaction(transactionId, vehicle, parkingSpot, ParkingStatus.ACTIVE, PaymentStatus.PENDING);

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(activeTransaction));
        when(parkingSpotRepository.save(argThat(spot -> spot.getStatus() == ParkingSpotStatus.AVAILABLE))).thenReturn(parkingSpot);
        when(parkingTransactionRepository.save(argThat(trans -> trans.getStatus() == ParkingStatus.CANCELLED))).thenReturn(activeTransaction);

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminCancelParkingTransaction(transactionId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("success", responseHandler.getMessage());
        assertNull(responseHandler.getData()); // No data expected for cancellation success

        verify(parkingTransactionRepository).findById(transactionId);
        verify(parkingSpotRepository).save(argThat(spot -> spot.getStatus() == ParkingSpotStatus.AVAILABLE));
        verify(parkingTransactionRepository).save(argThat(trans -> trans.getStatus() == ParkingStatus.CANCELLED));
        verifyNoInteractions(parkingTransactionResponseMapper);
    }

    @Test
    void adminCancelParkingTransaction_Success_ActiveTransactionCancelled_NoParkingSpot() {
        String transactionId = "t1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle("v1", "ABC1234", user);
        // Transaction without an assigned parking spot (e.g., failed to assign, or for a different type of parking)
        ParkingTransaction activeTransaction = createMockParkingTransaction(transactionId, vehicle, null, ParkingStatus.ACTIVE, PaymentStatus.PENDING);

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(activeTransaction));
        when(parkingTransactionRepository.save(argThat(trans -> trans.getStatus() == ParkingStatus.CANCELLED))).thenReturn(activeTransaction);

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminCancelParkingTransaction(transactionId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("success", responseHandler.getMessage());
        assertNull(responseHandler.getData());

        verify(parkingTransactionRepository).findById(transactionId);
        verify(parkingTransactionRepository).save(argThat(trans -> trans.getStatus() == ParkingStatus.CANCELLED));
        verifyNoInteractions(parkingSpotRepository, parkingTransactionResponseMapper); // parkingSpotRepository should not be called
    }


    @Test
    void adminCancelParkingTransaction_NotFound_TransactionNotFound() {
        String transactionId = "nonExistentId";

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminCancelParkingTransaction(transactionId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking transaction not found with ID: " + transactionId, responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoMoreInteractions(parkingTransactionRepository, parkingSpotRepository, parkingTransactionResponseMapper);
    }

    @Test
    void adminCancelParkingTransaction_BadRequest_AlreadyCompletedTransaction() {
        String transactionId = "t1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot parkingSpot = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.AVAILABLE);
        ParkingTransaction completedTransaction = createMockParkingTransaction(transactionId, vehicle, parkingSpot, ParkingStatus.COMPLETED, PaymentStatus.PAID);

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(completedTransaction));

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminCancelParkingTransaction(transactionId);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Completed transactions cannot be cancelled.", responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoMoreInteractions(parkingTransactionRepository, parkingSpotRepository, parkingTransactionResponseMapper);
    }

    @Test
    void adminCancelParkingTransaction_Conflict_AlreadyCancelledTransaction() {
        String transactionId = "t1";
        Users user = createMockUser("u1");
        Vehicle vehicle = createMockVehicle("v1", "ABC1234", user);
        Merchant merchant = createMockMerchant("m1", "MALL1");
        ParkingSpot parkingSpot = createMockParkingSpot("ps1", "A01", merchant, ParkingSpotStatus.AVAILABLE);
        ParkingTransaction cancelledTransaction = createMockParkingTransaction(transactionId, vehicle, parkingSpot, ParkingStatus.CANCELLED, PaymentStatus.FAILED);

        when(parkingTransactionRepository.findById(transactionId)).thenReturn(Optional.of(cancelledTransaction));

        ResponseEntity<Object> responseEntity = adminParkingTransactionService.adminCancelParkingTransaction(transactionId);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Transaction is already cancelled.", responseHandler.getMessage());

        verify(parkingTransactionRepository).findById(transactionId);
        verifyNoMoreInteractions(parkingTransactionRepository, parkingSpotRepository, parkingTransactionResponseMapper);
    }
}