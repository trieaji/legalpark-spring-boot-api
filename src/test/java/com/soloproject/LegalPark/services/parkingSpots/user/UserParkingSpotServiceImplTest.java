package com.soloproject.LegalPark.services.parkingSpots.user;

import com.soloproject.LegalPark.dto.request.parkingSpot.AvailableSpotFilterRequest;
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
import com.soloproject.LegalPark.service.parkingSpot.users.UserParkingSpotServiceImpl;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserParkingSpotServiceImplTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private ParkingSpotResponseMapper parkingSpotResponseMapper;

    @InjectMocks
    private UserParkingSpotServiceImpl userParkingSpotService;

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
    // TEST UNTUK METODE userGetAvailableParkingSpots
    // ====================================================================================================

    @Test
    void userGetAvailableParkingSpots_NoFilter_ReturnsAllAvailableSpots() {
        // 1. Persiapan Data Mock
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest(); // Filter kosong

        Merchant merchant1 = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        Merchant merchant2 = createMockMerchant(UUID.randomUUID().toString(), "MERCH002", "Merchant B");

        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant1);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "B02", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 2, merchant2);
        List<ParkingSpot> availableSpots = Arrays.asList(spot1, spot2);

        MerchantResponse merchantResp1 = createMockMerchantResponse(merchant1.getId(), merchant1.getMerchantCode(), merchant1.getMerchantName());
        MerchantResponse merchantResp2 = createMockMerchantResponse(merchant2.getId(), merchant2.getMerchantCode(), merchant2.getMerchantName());

        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp1);
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), "B02", "MOTORCYCLE", "AVAILABLE", 2, merchantResp2);


        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE)).thenReturn(availableSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot2)).thenReturn(spotResp2);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

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

        verify(parkingSpotRepository).findByStatus(ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
        verify(merchantRepository, never()).findByMerchantCode(anyString()); // Pastikan merchantRepository tidak dipanggil
    }

    @Test
    void userGetAvailableParkingSpots_FilterByMerchantCode_Success() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setMerchantCode(merchantCode);

        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");
        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "A02", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 1, merchant);
        List<ParkingSpot> availableSpots = Arrays.asList(spot1, spot2);

        MerchantResponse merchantResp = createMockMerchantResponse(merchant.getId(), merchant.getMerchantCode(), merchant.getMerchantName());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp);
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), "A02", "MOTORCYCLE", "AVAILABLE", 1, merchantResp);


        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE)).thenReturn(availableSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot2)).thenReturn(spotResp2);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

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

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotRepository, never()).findByStatus(any()); // Pastikan ini tidak dipanggil
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void userGetAvailableParkingSpots_FilterByMerchantCode_MerchantNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String merchantCode = "NONEXISTENT";
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setMerchantCode(merchantCode);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant not found with code: " + merchantCode, responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository, never()).findByMerchantAndStatus(any(), any());
        verify(parkingSpotRepository, never()).findByStatus(any());
    }

    @Test
    void userGetAvailableParkingSpots_FilterBySpotTypeOnly_Success() {
        // 1. Persiapan Data Mock
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setSpotType("CAR");

        Merchant merchant1 = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        Merchant merchant2 = createMockMerchant(UUID.randomUUID().toString(), "MERCH002", "Merchant B");

        // Available CAR spots
        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant1);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "B03", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 2, merchant2);
        // Not a CAR spot, but available
        ParkingSpot spot3 = createMockParkingSpot(UUID.randomUUID().toString(), "C01", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 1, merchant1);
        // CAR spot, but not available
        ParkingSpot spot4 = createMockParkingSpot(UUID.randomUUID().toString(), "D01", SpotType.CAR, ParkingSpotStatus.OCCUPIED, 1, merchant1);

        List<ParkingSpot> allAvailableSpots = Arrays.asList(spot1, spot3, spot2); // repo returns all available regardless of type first

        MerchantResponse merchantResp1 = createMockMerchantResponse(merchant1.getId(), merchant1.getMerchantCode(), merchant1.getMerchantName());
        MerchantResponse merchantResp2 = createMockMerchantResponse(merchant2.getId(), merchant2.getMerchantCode(), merchant2.getMerchantName());

        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp1);
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), "B03", "CAR", "AVAILABLE", 2, merchantResp2);


        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE)).thenReturn(allAvailableSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot2)).thenReturn(spotResp2);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingSpotResponse> dataList = (List<ParkingSpotResponse>) responseHandler.getData();
        assertEquals(2, dataList.size());
        assertEquals("CAR", dataList.get(0).getSpotType());
        assertEquals("CAR", dataList.get(1).getSpotType());

        verify(parkingSpotRepository).findByStatus(ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
        verify(merchantRepository, never()).findByMerchantCode(anyString());
        verify(parkingSpotRepository, never()).findByMerchantAndStatusAndSpotType(any(), any(), any()); // Pastikan ini tidak dipanggil
    }

    @Test
    void userGetAvailableParkingSpots_FilterBySpotTypeOnly_InvalidType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setSpotType("INVALID_TYPE");

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE)).thenReturn(Collections.emptyList()); // Repositori akan dipanggil duluan

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Invalid spot type: INVALID_TYPE", responseHandler.getMessage());

        verify(parkingSpotRepository).findByStatus(ParkingSpotStatus.AVAILABLE); // Initial call will still happen
        verify(merchantRepository, never()).findByMerchantCode(anyString());
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void userGetAvailableParkingSpots_FilterByFloorOnly_Success() {
        // 1. Persiapan Data Mock
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setFloor(1);

        Merchant merchant1 = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant1);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "B02", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 2, merchant1); // Different floor
        ParkingSpot spot3 = createMockParkingSpot(UUID.randomUUID().toString(), "C01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant1);

        List<ParkingSpot> allAvailableSpots = Arrays.asList(spot1, spot2, spot3);

        MerchantResponse merchantResp1 = createMockMerchantResponse(merchant1.getId(), merchant1.getMerchantCode(), merchant1.getMerchantName());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp1);
        ParkingSpotResponse spotResp3 = createMockParkingSpotResponse(spot3.getId(), "C01", "CAR", "AVAILABLE", 1, merchantResp1);

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE)).thenReturn(allAvailableSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot3)).thenReturn(spotResp3);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingSpotResponse> dataList = (List<ParkingSpotResponse>) responseHandler.getData();
        assertEquals(2, dataList.size());
        assertEquals(1, dataList.get(0).getFloor());
        assertEquals(1, dataList.get(1).getFloor());

        verify(parkingSpotRepository).findByStatus(ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void userGetAvailableParkingSpots_FilterByMerchantCodeAndSpotType_Success() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setMerchantCode(merchantCode);
        filter.setSpotType("CAR");

        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");
        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "A02", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 2, merchant);
        List<ParkingSpot> filteredSpots = Arrays.asList(spot1, spot2);

        MerchantResponse merchantResp = createMockMerchantResponse(merchant.getId(), merchant.getMerchantCode(), merchant.getMerchantName());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp);
        ParkingSpotResponse spotResp2 = createMockParkingSpotResponse(spot2.getId(), "A02", "CAR", "AVAILABLE", 2, merchantResp);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findByMerchantAndStatusAndSpotType(merchant, ParkingSpotStatus.AVAILABLE, SpotType.CAR)).thenReturn(filteredSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot2)).thenReturn(spotResp2);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingSpotResponse> dataList = (List<ParkingSpotResponse>) responseHandler.getData();
        assertEquals(2, dataList.size());
        assertEquals("CAR", dataList.get(0).getSpotType());
        assertEquals("CAR", dataList.get(1).getSpotType());
        assertEquals(merchantCode, dataList.get(0).getMerchant().getMerchantCode());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findByMerchantAndStatusAndSpotType(merchant, ParkingSpotStatus.AVAILABLE, SpotType.CAR);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void userGetAvailableParkingSpots_FilterByMerchantCodeAndSpotType_InvalidType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setMerchantCode(merchantCode);
        filter.setSpotType("INVALID_TYPE");

        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Invalid spot type: INVALID_TYPE", responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository, never()).findByMerchantAndStatusAndSpotType(any(), any(), any());
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }


    @Test
    void userGetAvailableParkingSpots_FilterByMerchantCodeAndFloor_Success() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setMerchantCode(merchantCode);
        filter.setFloor(1);

        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");
        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "A02", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 2, merchant); // Different floor
        ParkingSpot spot3 = createMockParkingSpot(UUID.randomUUID().toString(), "A03", SpotType.BICYCLE, ParkingSpotStatus.AVAILABLE, 1, merchant);

        List<ParkingSpot> allAvailableSpotsForMerchant = Arrays.asList(spot1, spot2, spot3); // repo returns all available regardless of floor

        MerchantResponse merchantResp = createMockMerchantResponse(merchant.getId(), merchant.getMerchantCode(), merchant.getMerchantName());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp);
        ParkingSpotResponse spotResp3 = createMockParkingSpotResponse(spot3.getId(), "A03", "BICYCLE", "AVAILABLE", 1, merchantResp);


        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE)).thenReturn(allAvailableSpotsForMerchant);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot3)).thenReturn(spotResp3);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingSpotResponse> dataList = (List<ParkingSpotResponse>) responseHandler.getData();
        assertEquals(2, dataList.size());
        assertEquals(1, dataList.get(0).getFloor());
        assertEquals(1, dataList.get(1).getFloor());
        assertEquals(merchantCode, dataList.get(0).getMerchant().getMerchantCode());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findByMerchantAndStatus(merchant, ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }


    @Test
    void userGetAvailableParkingSpots_FilterBySpotTypeAndFloor_Success() {
        // 1. Persiapan Data Mock
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setSpotType("CAR");
        filter.setFloor(1);

        Merchant merchant1 = createMockMerchant(UUID.randomUUID().toString(), "MERCH001", "Merchant A");
        Merchant merchant2 = createMockMerchant(UUID.randomUUID().toString(), "MERCH002", "Merchant B");

        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant1);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "B02", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 1, merchant2); // Wrong type
        ParkingSpot spot3 = createMockParkingSpot(UUID.randomUUID().toString(), "C03", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 2, merchant1); // Wrong floor
        ParkingSpot spot4 = createMockParkingSpot(UUID.randomUUID().toString(), "D04", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant2);

        List<ParkingSpot> allAvailableSpots = Arrays.asList(spot1, spot2, spot3, spot4);

        MerchantResponse merchantResp1 = createMockMerchantResponse(merchant1.getId(), merchant1.getMerchantCode(), merchant1.getMerchantName());
        MerchantResponse merchantResp2 = createMockMerchantResponse(merchant2.getId(), merchant2.getMerchantCode(), merchant2.getMerchantName());

        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp1);
        ParkingSpotResponse spotResp4 = createMockParkingSpotResponse(spot4.getId(), "D04", "CAR", "AVAILABLE", 1, merchantResp2);


        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE)).thenReturn(allAvailableSpots);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot4)).thenReturn(spotResp4);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingSpotResponse> dataList = (List<ParkingSpotResponse>) responseHandler.getData();
        assertEquals(2, dataList.size());
        assertEquals("CAR", dataList.get(0).getSpotType());
        assertEquals(1, dataList.get(0).getFloor());
        assertEquals("CAR", dataList.get(1).getSpotType());
        assertEquals(1, dataList.get(1).getFloor());

        verify(parkingSpotRepository).findByStatus(ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotResponseMapper, times(2)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }


    @Test
    void userGetAvailableParkingSpots_FilterByMerchantCodeSpotTypeAndFloor_Success() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setMerchantCode(merchantCode);
        filter.setSpotType("CAR");
        filter.setFloor(1);

        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");
        ParkingSpot spot1 = createMockParkingSpot(UUID.randomUUID().toString(), "A01", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 1, merchant);
        ParkingSpot spot2 = createMockParkingSpot(UUID.randomUUID().toString(), "A02", SpotType.CAR, ParkingSpotStatus.AVAILABLE, 2, merchant); // Wrong floor
        ParkingSpot spot3 = createMockParkingSpot(UUID.randomUUID().toString(), "A03", SpotType.MOTORCYCLE, ParkingSpotStatus.AVAILABLE, 1, merchant); // Wrong type

        List<ParkingSpot> filteredSpots = Arrays.asList(spot1); // Only spot1 matches all criteria

        MerchantResponse merchantResp = createMockMerchantResponse(merchant.getId(), merchant.getMerchantCode(), merchant.getMerchantName());
        ParkingSpotResponse spotResp1 = createMockParkingSpotResponse(spot1.getId(), "A01", "CAR", "AVAILABLE", 1, merchantResp);


        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        // repo initially returns spots matching merchant, status, and spotType
        when(parkingSpotRepository.findByMerchantAndStatusAndSpotType(merchant, ParkingSpotStatus.AVAILABLE, SpotType.CAR))
                .thenReturn(Arrays.asList(spot1, spot2)); // only spot1 and spot2 are CARs and AVAILABLE at this merchant
        when(parkingSpotResponseMapper.mapToParkingSpotResponse(spot1)).thenReturn(spotResp1);


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof List);
        List<ParkingSpotResponse> dataList = (List<ParkingSpotResponse>) responseHandler.getData();
        assertEquals(1, dataList.size());
        assertEquals("A01", dataList.get(0).getSpotNumber());
        assertEquals("CAR", dataList.get(0).getSpotType());
        assertEquals(1, dataList.get(0).getFloor());
        assertEquals(merchantCode, dataList.get(0).getMerchant().getMerchantCode());

        verify(merchantRepository).findByMerchantCode(merchantCode);
        verify(parkingSpotRepository).findByMerchantAndStatusAndSpotType(merchant, ParkingSpotStatus.AVAILABLE, SpotType.CAR);
        verify(parkingSpotResponseMapper, times(1)).mapToParkingSpotResponse(any(ParkingSpot.class));
    }


    @Test
    void userGetAvailableParkingSpots_ReturnsEmptyList_WhenNoSpotsMatchFilter() {
        // 1. Persiapan Data Mock
        AvailableSpotFilterRequest filter = new AvailableSpotFilterRequest();
        filter.setSpotType("BICYCLE"); // Filter untuk spot yang tidak ada

        // 2. Mock Perilaku Dependensi
        when(parkingSpotRepository.findByStatus(ParkingSpotStatus.AVAILABLE)).thenReturn(Collections.emptyList());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetAvailableParkingSpots(filter);

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
        assertTrue(dataList.isEmpty()); // Should be empty

        verify(parkingSpotRepository).findByStatus(ParkingSpotStatus.AVAILABLE);
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }


    // ====================================================================================================
    // TEST UNTUK METODE userGetParkingSpotsByMerchant
    // ====================================================================================================

    @Test
    void userGetParkingSpotsByMerchant_Success() {
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
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetParkingSpotsByMerchant(merchantCode);

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
    void userGetParkingSpotsByMerchant_MerchantNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentMerchantCode = "NONEXISTENT";

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(nonExistentMerchantCode)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetParkingSpotsByMerchant(nonExistentMerchantCode);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant not found with code: " + nonExistentMerchantCode, responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(nonExistentMerchantCode);
        verify(parkingSpotRepository, never()).findByMerchant(any());
        verify(parkingSpotResponseMapper, never()).mapToParkingSpotResponse(any(ParkingSpot.class));
    }

    @Test
    void userGetParkingSpotsByMerchant_MerchantFoundButNoParkingSpots_ReturnsEmptyList() {
        // 1. Persiapan Data Mock
        String merchantCode = "MERCH001";
        Merchant merchant = createMockMerchant(UUID.randomUUID().toString(), merchantCode, "Merchant A");

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(merchant));
        when(parkingSpotRepository.findByMerchant(merchant)).thenReturn(Collections.emptyList());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userParkingSpotService.userGetParkingSpotsByMerchant(merchantCode);

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