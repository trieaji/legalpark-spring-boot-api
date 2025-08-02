package com.soloproject.LegalPark.services.vehicles.admin;

import com.soloproject.LegalPark.dto.request.vehicle.VehicleRequest;
import com.soloproject.LegalPark.dto.response.users.UserBasicResponse;
import com.soloproject.LegalPark.dto.response.vehicle.VehicleResponse;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.entity.Vehicle;
import com.soloproject.LegalPark.entity.VehicleType;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.VehicleResponseMapper;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.repository.VehicleRepository;
import com.soloproject.LegalPark.service.vehicle.admin.AdminVehicleServiceImpl;
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
public class AdminVehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private UsersRepository usersRepository; // Digunakan untuk adminRegisterVehicle & adminGetVehiclesByUserId
    @Mock
    private VehicleResponseMapper vehicleResponseMapper;

    // InfoAccount tidak perlu di-mock karena tidak digunakan di service ini
    // MerchantRepository tidak perlu di-mock karena logikanya dikomentari di service

    @InjectMocks
    private AdminVehicleServiceImpl adminVehicleService;

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
    private Users createMockUser(String id, String email, String username) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        user.setAccountName(username);
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
    // TEST UNTUK METODE adminRegisterVehicle
    // ====================================================================================================

    @Test
    void adminRegisterVehicle_Success() {
        // 1. Persiapan Data Mock
        String ownerId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B5678DEF");
        request.setType("MOTORCYCLE");
        request.setOwnerId(ownerId);

        Users owner = createMockUser(ownerId, "owner@example.com", "testowner");
        Vehicle newVehicle = createMockVehicle(UUID.randomUUID().toString(), request.getLicensePlate(), VehicleType.MOTORCYCLE, owner);

        UserBasicResponse ownerBasicResponse = createMockUserBasicResponse(ownerId, owner.getAccountName(), owner.getEmail());
        VehicleResponse expectedResponse = createMockVehicleResponse(newVehicle.getId(), newVehicle.getLicensePlate(), newVehicle.getType().name(), ownerBasicResponse);

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(vehicleRepository.findByLicensePlate(request.getLicensePlate())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(newVehicle);
        when(vehicleResponseMapper.mapToVehicleResponse(newVehicle)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminRegisterVehicle(request);

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
        assertEquals(expectedResponse.getType(), resultResponse.getType());
        assertNotNull(resultResponse.getOwner());
        assertEquals(ownerId, resultResponse.getOwner().getId());

        // Verifikasi panggilan
        verify(usersRepository).findById(ownerId);
        verify(vehicleRepository).findByLicensePlate(request.getLicensePlate());
        verify(vehicleRepository).save(argThat(vehicle ->
                vehicle.getLicensePlate().equals(request.getLicensePlate()) &&
                        vehicle.getType().equals(VehicleType.MOTORCYCLE) &&
                        vehicle.getOwner().equals(owner)
        ));
        verify(vehicleResponseMapper).mapToVehicleResponse(newVehicle);
    }

    @Test
    void adminRegisterVehicle_OwnerIdMissing_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B5678DEF");
        request.setType("MOTORCYCLE");
        request.setOwnerId(null); // Owner ID missing

        // 2. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminRegisterVehicle(request);

        // 3. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Owner ID is required for admin to create vehicle.", responseHandler.getMessage());

        verify(usersRepository, never()).findById(anyString()); // Pastikan tidak ada interaksi lain
        verify(vehicleRepository, never()).findByLicensePlate(anyString());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void adminRegisterVehicle_OwnerNotFound_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String nonExistentOwnerId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B5678DEF");
        request.setType("MOTORCYCLE");
        request.setOwnerId(nonExistentOwnerId);

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(nonExistentOwnerId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Owner not found with ID: " + nonExistentOwnerId, responseHandler.getMessage());

        verify(usersRepository).findById(nonExistentOwnerId);
        verify(vehicleRepository, never()).findByLicensePlate(anyString());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void adminRegisterVehicle_LicensePlateAlreadyExists_ReturnsConflict() {
        // 1. Persiapan Data Mock
        String ownerId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B5678DEF");
        request.setType("MOTORCYCLE");
        request.setOwnerId(ownerId);

        Users owner = createMockUser(ownerId, "owner@example.com", "testowner");
        Vehicle existingVehicle = createMockVehicle(UUID.randomUUID().toString(), request.getLicensePlate(), VehicleType.CAR, owner);

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(vehicleRepository.findByLicensePlate(request.getLicensePlate())).thenReturn(Optional.of(existingVehicle));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Vehicle with this license plate is already registered.", responseHandler.getMessage());

        verify(usersRepository).findById(ownerId);
        verify(vehicleRepository).findByLicensePlate(request.getLicensePlate());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void adminRegisterVehicle_InvalidVehicleType_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String ownerId = UUID.randomUUID().toString();
        VehicleRequest request = new VehicleRequest();
        request.setLicensePlate("B5678DEF");
        request.setType("INVALIdTYPE"); // Tipe tidak valid
        request.setOwnerId(ownerId);

        Users owner = createMockUser(ownerId, "owner@example.com", "testowner");

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(vehicleRepository.findByLicensePlate(request.getLicensePlate())).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminRegisterVehicle(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertTrue(responseHandler.getMessage().contains("Invalid vehicle type provided: INVALIDTYPE"));

        verify(usersRepository).findById(ownerId);
        verify(vehicleRepository).findByLicensePlate(request.getLicensePlate());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminGetAllVehicles
    // ====================================================================================================

    @Test
    void adminGetAllVehicles_ReturnsListOfVehicles() {
        // 1. Persiapan Data Mock
        Users user1 = createMockUser(UUID.randomUUID().toString(), "user1@example.com", "user1");
        Users user2 = createMockUser(UUID.randomUUID().toString(), "user2@example.com", "user2");

        Vehicle vehicle1 = createMockVehicle(UUID.randomUUID().toString(), "B1111AAA", VehicleType.CAR, user1);
        Vehicle vehicle2 = createMockVehicle(UUID.randomUUID().toString(), "D2222BBB", VehicleType.MOTORCYCLE, user2);
        List<Vehicle> vehicles = Arrays.asList(vehicle1, vehicle2);

        UserBasicResponse user1Basic = createMockUserBasicResponse(user1.getId(), user1.getAccountName(), user1.getEmail());
        UserBasicResponse user2Basic = createMockUserBasicResponse(user2.getId(), user2.getAccountName(), user2.getEmail());

        VehicleResponse response1 = createMockVehicleResponse(vehicle1.getId(), "B1111AAA", "CAR", user1Basic);
        VehicleResponse response2 = createMockVehicleResponse(vehicle2.getId(), "D2222BBB", "MOTORCYCLE", user2Basic);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle1)).thenReturn(response1);
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle2)).thenReturn(response2);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetAllVehicles();

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
    void adminGetAllVehicles_ReturnsEmptyList_WhenNoVehiclesExist() {
        // 1. Persiapan Data Mock (list kosong)
        List<Vehicle> emptyList = Arrays.asList();

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findAll()).thenReturn(emptyList);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetAllVehicles();

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
    // TEST UNTUK METODE adminGetVehicleById
    // ====================================================================================================

    @Test
    void adminGetVehicleById_Success() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        Users owner = createMockUser(UUID.randomUUID().toString(), "owner@example.com", "ownername");
        Vehicle vehicle = createMockVehicle(vehicleId, "E5555FFF", VehicleType.TRUCK, owner);
        UserBasicResponse ownerBasic = createMockUserBasicResponse(owner.getId(), owner.getAccountName(), owner.getEmail());
        VehicleResponse expectedResponse = createMockVehicleResponse(vehicleId, "E5555FFF", "TRUCK", ownerBasic);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle)).thenReturn(expectedResponse);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetVehicleById(vehicleId);

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
    void adminGetVehicleById_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetVehicleById(nonExistentId);

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
    // TEST UNTUK METODE adminGetVehiclesByUserId
    // ====================================================================================================

    @Test
    void adminGetVehiclesByUserId_Success() {
        // 1. Persiapan Data Mock
        String userId = UUID.randomUUID().toString();
        Users targetUser = createMockUser(userId, "target@example.com", "targetuser");

        Vehicle vehicle1 = createMockVehicle(UUID.randomUUID().toString(), "ABC111", VehicleType.CAR, targetUser);
        Vehicle vehicle2 = createMockVehicle(UUID.randomUUID().toString(), "XYZ222", VehicleType.MOTORCYCLE, targetUser);
        List<Vehicle> userVehicles = Arrays.asList(vehicle1, vehicle2);

        UserBasicResponse userBasic = createMockUserBasicResponse(userId, targetUser.getAccountName(), targetUser.getEmail());
        VehicleResponse response1 = createMockVehicleResponse(vehicle1.getId(), "ABC111", "CAR", userBasic);
        VehicleResponse response2 = createMockVehicleResponse(vehicle2.getId(), "XYZ222", "MOTORCYCLE", userBasic);

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(vehicleRepository.findByOwner(targetUser)).thenReturn(userVehicles);
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle1)).thenReturn(response1);
        when(vehicleResponseMapper.mapToVehicleResponse(vehicle2)).thenReturn(response2);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetVehiclesByUserId(userId);

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
        assertEquals("ABC111", res1.getLicensePlate());
        assertEquals("CAR", res1.getType());

        verify(usersRepository).findById(userId);
        verify(vehicleRepository).findByOwner(targetUser);
        verify(vehicleResponseMapper, times(2)).mapToVehicleResponse(any(Vehicle.class));
    }

    @Test
    void adminGetVehiclesByUserId_UserNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentUserId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetVehiclesByUserId(nonExistentUserId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("User not found with ID: " + nonExistentUserId, responseHandler.getMessage());

        verify(usersRepository).findById(nonExistentUserId);
        verify(vehicleRepository, never()).findByOwner(any(Users.class));
        verify(vehicleResponseMapper, never()).mapToVehicleResponse(any(Vehicle.class));
    }

    @Test
    void adminGetVehiclesByUserId_ReturnsEmptyList_WhenUserHasNoVehicles() {
        // 1. Persiapan Data Mock
        String userId = UUID.randomUUID().toString();
        Users targetUser = createMockUser(userId, "target@example.com", "targetuser");
        List<Vehicle> emptyVehicleList = Arrays.asList();

        // 2. Mock Perilaku Dependensi
        when(usersRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(vehicleRepository.findByOwner(targetUser)).thenReturn(emptyVehicleList);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminGetVehiclesByUserId(userId);

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

        verify(usersRepository).findById(userId);
        verify(vehicleRepository).findByOwner(targetUser);
        verify(vehicleResponseMapper, never()).mapToVehicleResponse(any(Vehicle.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE adminDeleteVehicle
    // ====================================================================================================

    @Test
    void adminDeleteVehicle_Success() {
        // 1. Persiapan Data Mock
        String vehicleId = UUID.randomUUID().toString();
        Users owner = createMockUser(UUID.randomUUID().toString(), "owner@example.com", "ownername");
        Vehicle vehicleToDelete = createMockVehicle(vehicleId, "X1234YZ", VehicleType.CAR, owner);

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleToDelete));
        doNothing().when(vehicleRepository).delete(vehicleToDelete); // Mocking void method

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminDeleteVehicle(vehicleId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertEquals("Vehicle with ID " + vehicleId + " has been deleted successfully.", responseHandler.getData()); // Mengembalikan String

        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository).delete(vehicleToDelete);
    }

    @Test
    void adminDeleteVehicle_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(vehicleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = adminVehicleService.adminDeleteVehicle(nonExistentId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Vehicle not found with ID: " + nonExistentId, responseHandler.getMessage());

        verify(vehicleRepository).findById(nonExistentId);
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }
}