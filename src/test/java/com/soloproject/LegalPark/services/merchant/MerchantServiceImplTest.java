package com.soloproject.LegalPark.services.merchant;

import com.soloproject.LegalPark.dto.request.merchant.MerchantRequest;
import com.soloproject.LegalPark.dto.response.merchant.MerchantResponse;
import com.soloproject.LegalPark.entity.Merchant;
import com.soloproject.LegalPark.exception.ResponseHandler; // Static helper
import com.soloproject.LegalPark.helper.CodeGeneratorUtil;
import com.soloproject.LegalPark.repository.MerchantRepository;
import com.soloproject.LegalPark.service.merchant.MerchantServiceImpl;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MerchantServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private CodeGeneratorUtil codeGeneratorUtil;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    // Untuk mock static methods ResponseHandler
    private MockedStatic<ResponseHandler> mockedResponseHandler;

    @BeforeEach
    void setUp() {
        // Mock static methods for ResponseHandler
        mockedResponseHandler = Mockito.mockStatic(ResponseHandler.class);
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
        // Close the static mocks after each test
        mockedResponseHandler.close();
    }

    // ====================================================================================================
    // TEST UNTUK METODE createNewMerchant
    // ====================================================================================================

    @Test
    void createNewMerchant_Success() {
        // 1. Persiapan Data Mock
        MerchantRequest request = new MerchantRequest();
        request.setMerchantName("Test Merchant");
        request.setMerchantAddress("123 Test St");
        request.setContactPerson("John Doe");
        request.setContactPhone("1234567890");

        String generatedCode = "ABC123XYZ";
        String newMerchantId = UUID.randomUUID().toString();

        Merchant savedMerchant = new Merchant();
        savedMerchant.setId(newMerchantId);
        savedMerchant.setMerchantCode(generatedCode);
        savedMerchant.setMerchantName(request.getMerchantName());
        savedMerchant.setMerchantAddress(request.getMerchantAddress());
        savedMerchant.setContactPerson(request.getContactPerson());
        savedMerchant.setContactPhone(request.getContactPhone());
        savedMerchant.setCreatedAt(LocalDateTime.now());
        savedMerchant.setUpdatedAt(LocalDateTime.now());

        // 2. Mock Perilaku Dependensi
        when(codeGeneratorUtil.generateUniqueMerchantShortCode()).thenReturn(generatedCode);
        when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.createNewMerchant(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);

        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof MerchantResponse);
        MerchantResponse merchantResponse = (MerchantResponse) responseHandler.getData();
        assertEquals(newMerchantId, merchantResponse.getId());
        assertEquals(generatedCode, merchantResponse.getMerchantCode());
        assertEquals(request.getMerchantName(), merchantResponse.getMerchantName());
        assertEquals(request.getMerchantAddress(), merchantResponse.getMerchantAddress());
        assertEquals(request.getContactPerson(), merchantResponse.getContactPerson());
        assertEquals(request.getContactPhone(), merchantResponse.getContactPhone());

        // Verifikasi bahwa dependensi dipanggil dengan benar
        verify(codeGeneratorUtil).generateUniqueMerchantShortCode();
        verify(merchantRepository).save(any(Merchant.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE getAllMerchants
    // ====================================================================================================

    @Test
    void getAllMerchants_ReturnsListOfMerchants() {
        // 1. Persiapan Data Mock
        Merchant merchant1 = new Merchant();
        merchant1.setId(UUID.randomUUID().toString());
        merchant1.setMerchantName("Merchant A");
        merchant1.setMerchantCode("CODEA");

        Merchant merchant2 = new Merchant();
        merchant2.setId(UUID.randomUUID().toString());
        merchant2.setMerchantName("Merchant B");
        merchant2.setMerchantCode("CODEB");

        List<Merchant> merchantList = Arrays.asList(merchant1, merchant2);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findAll()).thenReturn(merchantList);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.getAllMerchants();

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
        assertTrue(dataList.get(0) instanceof Merchant); // Memverifikasi tipe data
        assertEquals("Merchant A", ((Merchant) dataList.get(0)).getMerchantName());

        verify(merchantRepository).findAll();
    }

    @Test
    void getAllMerchants_ReturnsEmptyList_WhenNoMerchantsExist() {
        // 1. Persiapan Data Mock (list kosong)
        List<Merchant> emptyList = Arrays.asList();

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findAll()).thenReturn(emptyList);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.getAllMerchants();

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

        verify(merchantRepository).findAll();
    }

    // ====================================================================================================
    // TEST UNTUK METODE deleteMerchant
    // ====================================================================================================

    @Test
    void deleteMerchant_Success() {
        // 1. Persiapan Data Mock
        String merchantId = UUID.randomUUID().toString();
        Merchant merchantToDelete = new Merchant();
        merchantToDelete.setId(merchantId);
        merchantToDelete.setMerchantName("Merchant to Delete");

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchantToDelete));
        doNothing().when(merchantRepository).deleteById(merchantId);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.deleteMerchant(merchantId);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);

        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());
        assertTrue(responseHandler.getData() instanceof Merchant); // Mengembalikan objek yang dihapus
        assertEquals(merchantId, ((Merchant) responseHandler.getData()).getId());

        verify(merchantRepository).findById(merchantId);
        verify(merchantRepository).deleteById(merchantId);
    }

    @Test
    void deleteMerchant_NotFound_ThrowsNoSuchElementException() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji & Verifikasi Exception
        assertThrows(NoSuchElementException.class, () -> merchantService.deleteMerchant(nonExistentId));

        verify(merchantRepository).findById(nonExistentId);
        verify(merchantRepository, never()).deleteById(anyString()); // Pastikan delete tidak dipanggil
    }

    // ====================================================================================================
    // TEST UNTUK METODE updateExistingMerchant
    // ====================================================================================================

    @Test
    void updateExistingMerchant_Success_AllFieldsUpdated() {
        // 1. Persiapan Data Mock
        String merchantId = UUID.randomUUID().toString();
        MerchantRequest request = new MerchantRequest();
        request.setMerchantName("Updated Name");
        request.setMerchantAddress("Updated Address");
        request.setContactPerson("Updated Person");
        request.setContactPhone("9876543210");

        Merchant existingMerchant = new Merchant();
        existingMerchant.setId(merchantId);
        existingMerchant.setMerchantName("Old Name");
        existingMerchant.setMerchantAddress("Old Address");
        existingMerchant.setContactPerson("Old Person");
        existingMerchant.setContactPhone("1111111111");
        existingMerchant.setMerchantCode("OLDCODE");

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setId(merchantId);
        updatedMerchant.setMerchantName(request.getMerchantName());
        updatedMerchant.setMerchantAddress(request.getMerchantAddress());
        updatedMerchant.setContactPerson(request.getContactPerson());
        updatedMerchant.setContactPhone(request.getContactPhone());
        updatedMerchant.setMerchantCode("OLDCODE"); // Code should not change

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(existingMerchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(updatedMerchant);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.updateExistingMerchant(merchantId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);

        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof Merchant);
        Merchant resultMerchant = (Merchant) responseHandler.getData();
        assertEquals(merchantId, resultMerchant.getId());
        assertEquals(request.getMerchantName(), resultMerchant.getMerchantName());
        assertEquals(request.getMerchantAddress(), resultMerchant.getMerchantAddress());
        assertEquals(request.getContactPerson(), resultMerchant.getContactPerson());
        assertEquals(request.getContactPhone(), resultMerchant.getContactPhone());
        assertEquals("OLDCODE", resultMerchant.getMerchantCode()); // Verify merchantCode is unchanged

        verify(merchantRepository).findById(merchantId);
        verify(merchantRepository).save(argThat(m ->
                m.getMerchantName().equals(request.getMerchantName()) &&
                        m.getMerchantAddress().equals(request.getMerchantAddress()) &&
                        m.getContactPerson().equals(request.getContactPerson()) &&
                        m.getContactPhone().equals(request.getContactPhone())
        ));
    }

    @Test
    void updateExistingMerchant_Success_PartialUpdate() {
        // 1. Persiapan Data Mock
        String merchantId = UUID.randomUUID().toString();
        MerchantRequest request = new MerchantRequest();
        request.setMerchantName("Only Name Updated");
        // request.setMerchantAddress(null); // Biarkan null
        // request.setContactPerson(null); // Biarkan null
        request.setContactPhone("9999999999"); // Update hanya phone

        Merchant existingMerchant = new Merchant();
        existingMerchant.setId(merchantId);
        existingMerchant.setMerchantName("Original Name");
        existingMerchant.setMerchantAddress("Original Address");
        existingMerchant.setContactPerson("Original Person");
        existingMerchant.setContactPhone("1111111111");
        existingMerchant.setMerchantCode("CODE123");

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setId(merchantId);
        updatedMerchant.setMerchantName(request.getMerchantName());
        updatedMerchant.setMerchantAddress(existingMerchant.getMerchantAddress()); // Should remain unchanged
        updatedMerchant.setContactPerson(existingMerchant.getContactPerson());   // Should remain unchanged
        updatedMerchant.setContactPhone(request.getContactPhone());              // Should be updated
        updatedMerchant.setMerchantCode(existingMerchant.getMerchantCode());

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(existingMerchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(updatedMerchant);

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.updateExistingMerchant(merchantId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());

        assertTrue(responseHandler.getData() instanceof Merchant);
        Merchant resultMerchant = (Merchant) responseHandler.getData();
        assertEquals(request.getMerchantName(), resultMerchant.getMerchantName()); // Updated
        assertEquals("Original Address", resultMerchant.getMerchantAddress()); // Unchanged
        assertEquals("Original Person", resultMerchant.getContactPerson());   // Unchanged
        assertEquals(request.getContactPhone(), resultMerchant.getContactPhone()); // Updated

        verify(merchantRepository).findById(merchantId);
        verify(merchantRepository).save(any(Merchant.class));
    }

    @Test
    void updateExistingMerchant_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentId = UUID.randomUUID().toString();
        MerchantRequest request = new MerchantRequest();
        request.setMerchantName("Any Name");

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.updateExistingMerchant(nonExistentId, request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant with ID " + nonExistentId + " not found.", responseHandler.getMessage());

        verify(merchantRepository).findById(nonExistentId);
        verify(merchantRepository, never()).save(any(Merchant.class));
    }

    // ====================================================================================================
    // TEST UNTUK METODE getMerchantByCode
    // ====================================================================================================

    @Test
    void getMerchantByCode_Success() {
        // 1. Persiapan Data Mock
        String merchantCode = "VALIDCODE";
        MerchantRequest request = new MerchantRequest();
        request.setMerchantCode(merchantCode);

        Merchant foundMerchant = new Merchant();
        foundMerchant.setId(UUID.randomUUID().toString());
        foundMerchant.setMerchantCode(merchantCode);
        foundMerchant.setMerchantName("Found Merchant");
        foundMerchant.setMerchantAddress("Found Address");
        foundMerchant.setContactPerson("Found Person");
        foundMerchant.setContactPhone("111222333");

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(merchantCode)).thenReturn(Optional.of(foundMerchant));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.getMerchantByCode(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);

        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof MerchantResponse);
        MerchantResponse merchantResponse = (MerchantResponse) responseHandler.getData();
        assertEquals(foundMerchant.getId(), merchantResponse.getId());
        assertEquals(foundMerchant.getMerchantCode(), merchantResponse.getMerchantCode());
        assertEquals(foundMerchant.getMerchantName(), merchantResponse.getMerchantName());
        assertEquals(foundMerchant.getMerchantAddress(), merchantResponse.getMerchantAddress());
        assertEquals(foundMerchant.getContactPerson(), merchantResponse.getContactPerson());
        assertEquals(foundMerchant.getContactPhone(), merchantResponse.getContactPhone());

        verify(merchantRepository).findByMerchantCode(merchantCode);
    }

    @Test
    void getMerchantByCode_NotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String nonExistentCode = "NONEXIST";
        MerchantRequest request = new MerchantRequest();
        request.setMerchantCode(nonExistentCode);

        // 2. Mock Perilaku Dependensi
        when(merchantRepository.findByMerchantCode(nonExistentCode)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = merchantService.getMerchantByCode(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);

        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getError());
        assertEquals("Merchant not found with code: " + nonExistentCode, responseHandler.getMessage());

        verify(merchantRepository).findByMerchantCode(nonExistentCode);
    }
}