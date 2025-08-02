package com.soloproject.LegalPark.services.parkingSpots.admin;

import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotRequest;
import com.soloproject.LegalPark.dto.request.parkingSpot.ParkingSpotUpdateRequest;
import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.dto.response.parkingSpot.ParkingSpotResponse;
import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.entity.ParkingSpot;
import com.soloproject.LegalPark.entity.ParkingSpotStatus;
import com.soloproject.LegalPark.entity.SpotType;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.ParkingSpotResponseMapper;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.repository.ParkingSpotRepository;
import com.soloproject.LegalPark.service.parkingSpot.admin.AdminParkingSpotServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminParkingSpotServiceImplTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private ParkingSpotResponseMapper parkingSpotResponseMapper;

    @InjectMocks
    private AdminParkingSpotServiceImpl adminParkingSpotService;

    private MockedStatic<ResponseHandler> mockedResponseHandler;

    @BeforeEach
    void setUp() {
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

    // Helper untuk membuat objek Merchant dummy
    private Merchant createMockMerchant(String id, String code, String name) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setMerchantCode(code);
        merchant.setMerchantName(name);
        return merchant;
    }

    // Helper untuk membuat objek ParkingSpot dummy
    private ParkingSpot createMockParkingSpot(String id, String spotNumber, SpotType spotType, ParkingSpotStatus status, Integer floor, Merchant merchant) {
        ParkingSpot spot = new ParkingSpot();
        spot.setId(id);
        spot.setSpotNumber(spotNumber);
        spot.setSpotType(spotType);
        spot.setStatus(status);
        spot.setFloor(floor);
        spot.setMerchant(merchant);
        spot.setCreatedAt(LocalDateTime.now());
        spot.setUpdatedAt(LocalDateTime.now());
        return spot;
    }

    // Helper untuk membuat objek ParkingSpotResponse dummy
    private ParkingSpotResponse createMockParkingSpotResponse(String id, String spotNumber, String spotType, String status, Integer floor, MerchantResponse merchantResponse) {
        ParkingSpotResponse response = new ParkingSpotResponse();
        response.setId(id);
        response.setSpotNumber(spotNumber);
        response.setSpotType(spotType);
        response.setStatus(status);
        response.setFloor(floor);
        response.setMerchant(merchantResponse);
        return response;
    }

    // Helper untuk membuat objek MerchantResponse dummy
    private MerchantResponse createMockMerchantResponse(String id, String code, String name) {
        MerchantResponse response = new MerchantResponse();
        response.setId(id);
        response.setMerchantCode(code);
        response.setMerchantName(name);
        return response;
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminCreateParkingSpot
    // ====================================================================================================

    @Test
    void adminCreateParkingSpot_Success() {
        // 1. Persiapan Data Mock
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");

        ParkingSpotRequest request = new ParkingSpotRequest();
        request.setSpotNumber("A01");
        request.setFloor(1);
        request.setSpotType("CAR");
        request.setMerchantCode(merchantCode);

        ParkingSpot newSpot = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        MerchantResponse merchantResp = createMockMerchantResponse(merchantId, merchantCode, "Merchant A");
        ParkingSpotResponse expectedResponse = createMockParkingSpotResponse(newSpot.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant)).thenReturn(Optional.empty());
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(newSpot);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(newSpot)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminCreateParkingSpot(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof ParkingSpotResponse);
        ParkingSpotResponse resultResponse = (ParkingSpotResponse) responseHandler.getData();
        assertEquals(expectedResponse.getId(), resultResponse.getId());
        assertEquals(expectedResponse.getSpotNumber(), resultResponse.getSpotNumber());
        assertEquals(expectedResponse.getSpotType(), resultResponse.getSpotType());
        assertEquals(expectedResponse.getStatus(), resultResponse.getStatus());
        assertEquals(expectedResponse.getMerchant().getMerchantCode(), resultResponse.getMerchant().getMerchantCode());

        // Verifikasi panggilan
        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(request.getSpotNumber(), merchant);
        verify(parkingSpotRepository).save(argThat(spot ->
                spot.getSpotNumber().equals(request.getSpotNumber()) &&
                        spot.getFloor().equals(request.getFloor()) &&
                        spot.getSpotType().equals(SpotType.CAR) &&
                        spot.getStatus().equals(ParkingSpotStatus.AVAILABLE) &&
                        spot.getMerchant().equals(merchant)
        ));
        verify(parkingSpotResponseMapper).mapToParkingSpotResponse(newSpot);
    }

    @Test
    void adminCreateParkingSpot_MerchantNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentMerchantCode = "NONEXISTENT";
        ParkingSpotRequest request = new ParkingSpotRequest();
        request.setMerchantCode(nonExistentMerchantCode); // Merchant code tidak ditemukan

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(nonExistentMerchantCode)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminCreateParkingSpot(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant not found with code: " + nonExistentMerchantCode, responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(nonExistentMerchantCode);
        verify(parkingSpotRepository, never()).findBySpotNumberAndMerchant(anyString(), any(Merchant.class));
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void adminCreateParkingSpot_ParkingSpotAlreadyExistsForMerchant_ReturnsConflict() {
        // 1. Persiapan Data Mock
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");

        ParkingSpotRequest request = new ParkingSpotRequest();
        request.setSpotNumber("A01");
        request.setFloor(1);
        request.setSpotType("CAR");
        request.setMerchantCode(merchantCode);

        ParkingSpot existingSpot = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant)).thenReturn(Optional.of(existingSpot));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminCreateParkingSpot(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking spot with number 'A01' already exists for this merchant.", responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(request.getSpotNumber(), merchant);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void adminCreateParkingSpot_InvalidSpotType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");

        ParkingSpotRequest request = new ParkingSpotRequest();
        request.setSpotNumber("A01");
        request.setFloor(1);
        request.setSpotType("INVALID_TYPE"); // Tipe tidak valid
        request.setMerchantCode(merchantCode);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminCreateParkingSpot(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Invalid spot type: INVALID_TYPE", responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(request.getSpotNumber(), merchant);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminGetAllParkingSpots
    // ====================================================================================================

    @Test
    void adminGetAllParkingSpots_ReturnsListOfParkingSpots() {
        // 1. Persiapan Data Mock
        Merchant merchant1 = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        Merchant merchant2 = createMockMerchant(UUID.randomUUID().toString(), "MERCH002", "Merchant B");

        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant1);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "B02", SpotType.MOTORCYCLE, ParkingSpotStatus.OCCUPIED, 2, merchant2);
        List<ParkingSpot> parkingSpots = Arrays.asList(spot1, spot2);

        MerchantResponse merchantResp1 = createMockMerchantResponse(merchant1.getId(), merchant1.getMerchantCode(), merchant1.getMerchantName());
        MerchantResponse merchantResp2 = createMockMerchantResponse(merchant2.getId(), merchant2.getMerchantCode(), merchant2.getMerchantName());

        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp1);
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), "B02", "MOTORCYCLE", "OCCUPIED", 2, merchantResp2);


        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findAll()).thenReturn(parkingSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot2)).thenReturn(spotResp2);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetAllParkingSpots();

        // 4. Verifikasi Hasil
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
        assertTrue(dataList.get(0) instanceof ParkingSpotResponse);

        verify(parkingSpotRepository).findAll();
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void adminGetAllParkingSpots_ReturnsEmptyList_WhenNoParkingSpotsExist() {
        // 1. Persiapan Data Mock (list kosong)
        List<ParkingSpot> emptyList = Collections.emptyList();

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findAll()).thenReturn(emptyList);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetAllParkingSpots();

        // 4. Verifikasi Hasil
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

        verify(parkingSpotRepository).findAll();
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminGetParkingSpotById
    // ====================================================================================================

    @Test
    void adminGetParkingSpotById_Success() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        ParkingSpot parkingSpot = createMockParkingSpot(spotId, "C03", SpotType.BICYCLE, ParkingSpotStatus.MAINTENANCE, 3, merchant);
        MerchantResponse merchantResp = createMockMerchantResponse(merchant.getId(), merchant.getMerchantCode(), merchant.getMerchantName());
        ParkingSpotResponse expectedResponse = createMockParkingSpotResponse(spotId, "C03", "BICYCLE", "MAINTENANCE", 3, merchantResp);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(parkingSpot));
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(parkingSpot)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetParkingSpotById(spotId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof ParkingSpotResponse);
        ParkingSpotResponse resultResponse = (ParkingSpotResponse) responseHandler.getData();
        assertEquals(expectedResponse.getId(), resultResponse.getId());
        assertEquals(expectedResponse.getSpotNumber(), resultResponse.getSpotNumber());

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotResponseMapper).mapToParkingSpotResponse(parkingSpot);
    }

    @Test
    void adminGetParkingSpotById_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetParkingSpotById(nonExistentId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking spot not found with ID: " + nonExistentId, responseHandler.getMessage());

        verify(parkingSpotRepository).findById(nonExistentId);
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminUpdateParkingSpot
    // ====================================================================================================

    @Test
    void adminUpdateParkingSpot_Success_UpdateSpotNumberAndFloor() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");
        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);

        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setSpotNumber("Z99");
        request.setFloor(5);

        ParkingSpot updatedSpot = createMockParkingSpot(spotId, "Z99", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 5, merchant);
        MerchantResponse merchantResp = createMockMerchantResponse(merchantId, merchantCode, "Merchant A");
        ParkingSpotResponse expectedResponse = createMockParkingSpotResponse(spotId, "Z99", "CAR", "AVAILABLE", 5, merchantResp);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));
        // Ketika spotNumber diubah, cek dulu apakah ada spot lain dengan nomor itu di merchant yang sama
        when(parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant)).thenReturn(Optional.empty());
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(updatedSpot);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(updatedSpot)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof ParkingSpotResponse);
        ParkingSpotResponse resultResponse = (ParkingSpotResponse) responseHandler.getData();
        assertEquals(expectedResponse.getSpotNumber(), resultResponse.getSpotNumber());
        assertEquals(expectedResponse.getFloor(), resultResponse.getFloor());

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant("Z99", merchant);
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(parkingSpotResponseMapper).mapToParkingSpotResponse(updatedSpot);
    }

    @Test
    void adminUpdateParkingSpot_Success_UpdateSpotTypeAndStatus() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");
        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);

        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setSpotType("MOTORCYCLE");
        request.setStatus("OCCUPIED");

        ParkingSpot updatedSpot = createMockParkingSpot(spotId, "A01", SpotType.MOTORCYCLE, ParkingSpotStatus.OCCUPIED, 1, merchant);
        MerchantResponse merchantResp = createMockMerchantResponse(merchantId, merchantCode, "Merchant A");
        ParkingSpotResponse expectedResponse = createMockParkingSpotResponse(spotId, "A01", "MOTORCYCLE", "OCCUPIED", 1, merchantResp);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(updatedSpot);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(updatedSpot)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof ParkingSpotResponse);
        ParkingSpotResponse resultResponse = (ParkingSpotResponse) responseHandler.getData();
        assertEquals(expectedResponse.getSpotType(), resultResponse.getSpotType());
        assertEquals(expectedResponse.getStatus(), resultResponse.getStatus());

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotRepository, never()).findBySpotNumberAndMerchant(anyString(), any(Merchant.class)); // Spot number not changed
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(parkingSpotResponseMapper).mapToParkingSpotResponse(updatedSpot);
    }

    @Test
    void adminUpdateParkingSpot_Success_UpdateMerchantCode() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String oldMerchantId = UUID.randomUUID().toString();
        String oldMerchantCode = "OLDMERCH";
        Merchant oldMerchant = createMockMerchant(oldMerchantId, oldMerchantCode, "Old Merchant");
        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, oldMerchant);

        String newMerchantId = UUID.randomUUID().toString();
        String newMerchantCode = "NEWMERCH";
        Merchant newMerchant = createMockMerchant(newMerchantId, newMerchantCode, "New Merchant");

        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setMerchantCode(newMerchantCode);

        ParkingSpot updatedSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, newMerchant);
        MerchantResponse newMerchantResp = createMockMerchantResponse(newMerchantId, newMerchantCode, "New Merchant");
        ParkingSpotResponse expectedResponse = createMockParkingSpotResponse(spotId, "A01", "CAR", "AVAILABLE", 1, newMerchantResp);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));
        when(merchantRepository.findByMerchantCode(newMerchantCode)).thenReturn(Optional.of(newMerchant));
        when(parkingSpotRepository.findBySpotNumberAndMerchant(existingSpot.getSpotNumber(), newMerchant)).thenReturn(Optional.empty()); // Check uniqueness in new merchant
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(updatedSpot);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(updatedSpot)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof ParkingSpotResponse);
        ParkingSpotResponse resultResponse = (ParkingSpotResponse) responseHandler.getData();
        assertEquals(expectedResponse.getMerchant().getMerchantCode(), resultResponse.getMerchant().getMerchantCode());

        verify(parkingSpotRepository).findById(spotId);
        verify(merchantRepository).findByMerchantCode(newMerchantCode);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(existingSpot.getSpotNumber(), newMerchant);
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(parkingSpotResponseMapper).mapToParkingSpotResponse(updatedSpot);
    }

    @Test
    void adminUpdateParkingSpot_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();
        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setSpotNumber("X01");

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(nonExistentId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking spot not found with ID: " + nonExistentId, responseHandler.getMessage());

        verify(parkingSpotRepository).findById(nonExistentId);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void adminUpdateParkingSpot_SpotNumberAlreadyExistsInSameMerchant_ReturnsConflict() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String anotherSpotId = UUID.randomUUID().toString(); // ID spot lain dengan nomor yang sama
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");

        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot anotherSpot = createMockParkingSpot(anotherSpotId, "B02", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant); // Ini yang akan konflik

        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setSpotNumber("B02"); // Mengubah A01 menjadi B02, padahal B02 sudah ada

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));
        when(parkingSpotRepository.findBySpotNumberAndMerchant(request.getSpotNumber(), merchant)).thenReturn(Optional.of(anotherSpot));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking spot with number 'B02' already exists for this merchant.", responseHandler.getMessage());

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotRepository).findBySpotNumberAndMerchant(request.getSpotNumber(), merchant);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void adminUpdateParkingSpot_InvalidSpotType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");
        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);

        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setSpotType("BAD_TYPE");

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Invalid spot type: BAD_TYPE", responseHandler.getMessage());

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void adminUpdateParkingSpot_InvalidStatus_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String merchantId = UUID.randomUUID().toString();
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(merchantId, merchantCode, "Merchant A");
        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);

        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setStatus("BAD_STATUS");

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Invalid status: BAD_STATUS", responseHandler.getMessage());

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void adminUpdateParkingSpot_NewMerchantNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        String oldMerchantId = UUID.randomUUID().toString();
        String oldMerchantCode = "OLDMERCH";
        Merchant oldMerchant = createMockMerchant(oldMerchantId, oldMerchantCode, "Old Merchant");
        ParkingSpot existingSpot = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, oldMerchant);

        String nonExistentNewMerchantCode = "NONEXISTENT";
        ParkingSpotUpdateRequest request = new ParkingSpotUpdateRequest();
        request.setMerchantCode(nonExistentNewMerchantCode);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(existingSpot));
        when(merchantRepository.findByMerchantCode(nonExistentNewMerchantCode)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminUpdateParkingSpot(spotId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
    }


    // ====================================================================================================
    // TEST UNTUK METODE adminDeleteParkingSpot
    // ====================================================================================================

    @Test
    void adminDeleteParkingSpot_Success() {
        // 1. Persiapan Data Mock
        String spotId = UUID.randomUUID().toString();
        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        ParkingSpot parkingSpotToDelete = createMockParkingSpot(spotId, "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(spotId)).thenReturn(Optional.of(parkingSpotToDelete));
        doNothing().when(parkingSpotRepository).deleteById(spotId); // Mocking void method

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminDeleteParkingSpot(spotId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNull(responseHandler.getData()); // Mengembalikan null untuk success delete

        verify(parkingSpotRepository).findById(spotId);
        verify(parkingSpotRepository).deleteById(spotId);
    }

    @Test
    void adminDeleteParkingSpot_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminDeleteParkingSpot(nonExistentId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Parking spot not found with ID: " + nonExistentId, responseHandler.getMessage());

        verify(parkingSpotRepository).findById(nonExistentId);
        verify(parkingSpotRepository, never()).deleteById(anyString());
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminGetParkingSpotsByMerchant
    // ====================================================================================================

    @Test
    void adminGetParkingSpotsByMerchant_Success() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");

        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "A02", SpotType.MOTORCYCLE, ParkingSpotStatus.OCCUPIED, 1, merchant);
        List<ParkingSpot> parkingSpots = Arrays.asList(spot1, spot2);

        MerchantResponse merchantResp = createMockMerchantResponse(merchant.getId(), merchant.getMerchantCode(), merchant.getMerchantName());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp);
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), "A02", "MOTORCYCLE", "OCCUPIED", 1, merchantResp);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findByMerchant(merchant)).thenReturn(parkingSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot2)).thenReturn(spotResp2);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetParkingSpotsByMerchant(merchantCode);

        // 4. Verifikasi Hasil
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
        assertTrue(dataList.get(0) instanceof ParkingSpotResponse);

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findByMerchant(merchant);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void adminGetParkingSpotsByMerchant_MerchantNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentMerchantCode = "NONEXISTENT";

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(nonExistentMerchantCode)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetParkingSpotsByMerchant(nonExistentMerchantCode);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant not found with identifier: " + nonExistentMerchantCode, responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(nonExistentMerchantCode);
        verify(parkingSpotRepository, never()).findByMerchant(any());
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void adminGetParkingSpotsByMerchant_MerchantFoundButNoParkingSpots_ReturnsEmptyList() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findByMerchant(merchant)).thenReturn(Collections.emptyList());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminParkingSpotService.adminGetParkingSpotsByMerchant(merchantCode);

        // 4. Verifikasi Hasil
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

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findByMerchant(merchant);
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }
}