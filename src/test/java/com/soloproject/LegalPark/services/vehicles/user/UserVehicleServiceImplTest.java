package com.soloproject.LegalPark.services.vehicles.user;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.dto.request.vehicle.VehicleUpdateRequest;
import com.soloproject.LegalPark.dto.response.users.UserBasicResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.AccountStatus;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.entity.Vehicle;
import com.soloproject.LegalPark.entity.VehicleType;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.helper.VehicleResponseMapper;
import com.soloproject.LegalPark.repository.VehicleRepository;
import com.soloproject.LegalPark.service.vehicle.users.UserVehicleServiceImpl;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserVehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private InfoAccount infoAccount;
    @Mock
    private VehicleResponseMapper vehicleResponseMapper; // Ini akan di-mock
    // ModelMapper tidak perlu di-mock karena diinstansiasi di service atau di-mock melalui VehicleResponseMapper

    @InjectMocks
    private UserVehicleServiceImpl userVehicleService;

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

    // Helper untuk membuat objek Users dummy
    private Users createMockUser(String id, String email, AccountStatus status) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        user.setAccountName("User " + id);
        user.setAccountStatus(status);
        return user;
    }

    // Helper untuk membuat objek Vehicle dummy
    private Vehicle createMockVehicle(String id, String licensePlate, VehicleType type, Users owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setType(type);
        vehicle.setOwner(owner);
        return vehicle;
    }

    // Helper untuk membuat objek VehicleResponse dummy
    private VehicleResponse createMockVehicleResponse(String id, String licensePlate, String type, UserBasicResponse owner) {
        VehicleResponse response = new VehicleResponse();
        response.setId(id);
        response.setLicensePlate(licensePlate);
        response.setType(type);
        response.setOwner(owner);
        return response;
    }

    // Helper untuk membuat objek UserBasicResponse dummy
    private UserBasicResponse createMockUserBasicResponse(String id, String username, String email) {
        UserBasicResponse response = new UserBasicResponse();
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        return response;
    }

    // ====================================================================================================
    // TEST UNTUK METODE UserRegisterVehicle
    // ====================================================================================================

    @Test
    void UserRegisterVehicle_Success() {
        // 1. Persiapan Data Mock
        String userId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B1234ABC");
        request.setType("CAR");
        request.setOwnerId(userId); // OwnerId di request akan diabaikan karena infoAccount.get() yang dipakai

        Users currentUser = createMockUser(userId, "user@example.com", AccountStatus.ACTIVE);
        Vehicle newVehicle = createMockVehicle(UUID.randomUUID().toString(), "B1234ABC", VehicleType.CAR, currentUser);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser); // User terautentikasi & aktif
        when(vehicleRepository.findByLicensePlate(request.getLicensePlate())).thenReturn(Optional.empty()); // Plat unik
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(newVehicle);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof VehicleResponse);
        VehicleResponse vehicleResponse = (VehicleResponse) responseHandler.getData();
        assertEquals(newVehicle.getId(), vehicleResponse.getId());
        assertEquals(newVehicle.getLicensePlate(), vehicleResponse.getLicensePlate());
        assertEquals(newVehicle.getType().name(), vehicleResponse.getType());
        assertNotNull(vehicleResponse.getOwner());
        assertEquals(currentUser.getId(), vehicleResponse.getOwner().getId());

        // Verifikasi panggilan
        verify(infoAccount).get();
        verify(vehicleRepository).findByLicensePlate(request.getLicensePlate());
        verify(vehicleRepository).save(argThat(vehicle ->
                vehicle.getLicensePlate().equals(request.getLicensePlate()) &&
                        vehicle.getType().equals(VehicleType.CAR) &&
                        vehicle.getOwner().equals(currentUser)
        ));
    }

    @Test
    void UserRegisterVehicle_Unauthorized_ReturnsUnauthorized() {
        // 1. Persiapan Data Mock
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B1234ABC");

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(null); // User tidak terautentikasi

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("User not authenticated. Please log in.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository, never()).findByLicensePlate(anyString()); // Pastikan tidak ada interaksi lain
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserRegisterVehicle_AccountNotActive_ReturnsForbidden() {
        // 1. Persiapan Data Mock
        String userId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B1234ABC");

        Users currentUser = createMockUser(userId, "user@example.com", AccountStatus.PENDING_VERIFICATION); // Akun belum aktif

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Account is not active. Please verify your email first.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository, never()).findByLicensePlate(anyString());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserRegisterVehicle_LicensePlateAlreadyExists_ReturnsConflict() {
        // 1. Persiapan Data Mock
        String userId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B1234ABC");
        request.setType("CAR");

        Users currentUser = createMockUser(userId, "user@example.com", AccountStatus.ACTIVE);
        Vehicle existingVehicle = createMockVehicle(UUID.randomUUID().toString(), "B1234ABC", VehicleType.CAR, currentUser);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);
        when(vehicleRepository.findByLicensePlate(request.getLicensePlate())).thenReturn(Optional.of(existingVehicle)); // Plat sudah ada

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Vehicle with this license plate is already registered.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository).findByLicensePlate(request.getLicensePlate());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserRegisterVehicle_InvalidVehicleType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String userId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B1234ABC");
        request.setType("INVALIdTYPE"); // Tipe tidak valid

        Users currentUser = createMockUser(userId, "user@example.com", AccountStatus.ACTIVE);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);
        when(vehicleRepository.findByLicensePlate(request.getLicensePlate())).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertTrue(responseHandler.getMessage().contains("Invalid vehicle type provided: INVALIDTYPE"));

        verify(infoAccount).get();
        verify(vehicleRepository).findByLicensePlate(request.getLicensePlate());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE UserGetAllVehicle
    // ====================================================================================================

    @Test
    void UserGetAllVehicle_ReturnsListOfVehicles() {
        // 1. Persiapan Data Mock
        Users user1 = createMockUser(UUID.randomUUID().toString(), "user1@example.com", AccountStatus.ACTIVE);
        Users user2 = createMockUser(UUID.randomUUID().toString(), "user2@example.com", AccountStatus.ACTIVE);

        Vehicle vehicle1 = createMockVehicle(UUID.randomUUID().toString(), "B1111AAA", VehicleType.CAR, user1);
        Vehicle vehicle2 = createMockVehicle(UUID.randomUUID().toString(), "D2222BBB", VehicleType.MOTORCYCLE, user2);
        List<Vehicle> vehicles = Arrays.asList(vehicle1, vehicle2);

        UserBasicResponse user1Basic = createMockUserBasicResponse(user1.getId(), user1.getAccountName(), user1.getEmail());
        UserBasicResponse user2Basic = createMockUserBasicResponse(user2.getId(), user2.getAccountName(), user2.getEmail());

        VehicleResponse response1 = createMockVehicleResponse(vehicle1.getId(), "B1111AAA", "CAR", user1Basic);
        VehicleResponse response2 = createMockVehicleResponse(vehicle2.getId(), "D2222BBB", "MOTORCYCLE", user2Basic);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        // Penting: Mocking VehicleResponseMapper.mapToVehicleResponse
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle1)).thenReturn(response1);
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle2)).thenReturn(response2);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserGetAllVehicle();

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
        assertTrue(dataList.get(0) instanceof VehicleResponse);

        VehicleResponse res1 = (VehicleResponse) dataList.get(0);
        assertEquals("B1111AAA", res1.getLicensePlate());
        assertEquals("CAR", res1.getType());

        VehicleResponse res2 = (VehicleResponse) dataList.get(1);
        assertEquals("D2222BBB", res2.getLicensePlate());
        assertEquals("MOTORCYCLE", res2.getType());

        verify(vehicleRepository).findAll();
        verify(vehicleResponseMapper, times(2)).mapToVehicleResponse(any(Vehicle.class));
    }

    @Test
    void UserGetAllVehicle_ReturnsEmptyList_WhenNoVehiclesExist() {
        // 1. Persiapan Data Mock (list kosong)
        List<Vehicle> emptyList = Arrays.asList();

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findAll()).thenReturn(emptyList);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserGetAllVehicle();

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

        verify(vehicleRepository).findAll();
        verify(vehicleResponseMapper, never()).mapToVehicleResponse(any(Vehicle.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE UserGetVehicleById
    // ====================================================================================================

    @Test
    void UserGetVehicleById_Success() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        Users owner = createMockUser(UUID.randomUUID().toString(), "owner@example.com", AccountStatus.ACTIVE);
        Vehicle vehicle = createMockVehicle(vehicleId, "E5555FFF", VehicleType.TRUCK, owner);
        UserBasicResponse ownerBasic = createMockUserBasicResponse(owner.getId(), owner.getAccountName(), owner.getEmail());
        VehicleResponse expectedResponse = createMockVehicleResponse(vehicleId, "E5555FFF", "TRUCK", ownerBasic);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserGetVehicleById(vehicleId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof VehicleResponse);
        VehicleResponse resultResponse = (VehicleResponse) responseHandler.getData();
        assertEquals(expectedResponse.getId(), resultResponse.getId());
        assertEquals(expectedResponse.getLicensePlate(), resultResponse.getLicensePlate());

        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleResponseMapper).mapToVehicleResponse(vehicle);
    }

    @Test
    void UserGetVehicleById_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserGetVehicleById(nonExistentId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Vehicle not found with ID: " + nonExistentId, responseHandler.getMessage());

        verify(vehicleRepository).findById(nonExistentId);
        verify(vehicleResponseMapper, never()).mapToVehicleResponse(any(Vehicle.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE UserUpdateVehicle
    // ====================================================================================================

    @Test
    void UserUpdateVehicle_Success() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        String ownerId = UUID.randomUUID().toString();

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("MOTORCYCLE"); // Hanya update tipe

        Users currentUser = createMockUser(ownerId, "owner@example.com", AccountStatus.ACTIVE);
        Vehicle existingVehicle = createMockVehicle(vehicleId, "B9999XYZ", VehicleType.CAR, currentUser);

        // Vehicle setelah diupdate dan disimpan
        Vehicle updatedVehicle = createMockVehicle(vehicleId, "B9999XYZ", VehicleType.MOTORCYCLE, currentUser);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserUpdateVehicle(vehicleId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof VehicleResponse);
        VehicleResponse resultResponse = (VehicleResponse) responseHandler.getData();
        assertEquals(vehicleId, resultResponse.getId());
        assertEquals(existingVehicle.getLicensePlate(), resultResponse.getLicensePlate());
        assertEquals(VehicleType.MOTORCYCLE.name(), resultResponse.getType()); // Tipe harus berubah

        verify(infoAccount).get();
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository).save(argThat(v -> v.getType().equals(VehicleType.MOTORCYCLE))); // Verifikasi tipe di-save
    }

    @Test
    void UserUpdateVehicle_Unauthorized_ReturnsUnauthorized() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("CAR");

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(null); // User tidak terautentikasi

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserUpdateVehicle(vehicleId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseHandler.getCode());
        assertEquals("User not authenticated. Please log in.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository, never()).findById(anyString());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserUpdateVehicle_AccountNotActive_ReturnsForbidden() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        String ownerId = UUID.randomUUID().toString();
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("CAR");

        Users currentUser = createMockUser(ownerId, "user@example.com", AccountStatus.PENDING_VERIFICATION); // Akun belum aktif

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserUpdateVehicle(vehicleId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), responseHandler.getCode());
        assertEquals("Account is not active. Please verify your email first.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository, never()).findById(anyString());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserUpdateVehicle_VehicleNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();
        String ownerId = UUID.randomUUID().toString();
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("CAR");

        Users currentUser = createMockUser(ownerId, "owner@example.com", AccountStatus.ACTIVE);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);
        when(vehicleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserUpdateVehicle(nonExistentId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("Vehicle with ID " + nonExistentId + " not found.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository).findById(nonExistentId);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserUpdateVehicle_NotOwner_ReturnsForbidden() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        String ownerId = UUID.randomUUID().toString();
        String anotherUserId = UUID.randomUUID().toString();

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("CAR");

        Users currentUser = createMockUser(anotherUserId, "another@example.com", AccountStatus.ACTIVE); // User yang login BUKAN pemilik
        Users actualOwner = createMockUser(ownerId, "owner@example.com", AccountStatus.ACTIVE);
        Vehicle existingVehicle = createMockVehicle(vehicleId, "B9999XYZ", VehicleType.CAR, actualOwner); // Kendaraan dimiliki oleh actualOwner

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserUpdateVehicle(vehicleId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), responseHandler.getCode());
        assertEquals("You are not authorized to update this vehicle.", responseHandler.getMessage());

        verify(infoAccount).get();
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void UserUpdateVehicle_InvalidVehicleType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        String ownerId = UUID.randomUUID().toString();

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("INVALID_TYPE"); // Tipe tidak valid

        Users currentUser = createMockUser(ownerId, "owner@example.com", AccountStatus.ACTIVE);
        Vehicle existingVehicle = createMockVehicle(vehicleId, "B9999XYZ", VehicleType.CAR, currentUser);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(currentUser);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = userVehicleService.UserUpdateVehicle(vehicleId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertTrue(responseHandler.getMessage().contains("Invalid vehicle type provided: INVALID_TYPE"));

        verify(infoAccount).get();
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}