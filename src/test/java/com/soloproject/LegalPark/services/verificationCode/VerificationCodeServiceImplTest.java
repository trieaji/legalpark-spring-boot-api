package com.soloproject.LegalPark.services.verificationCode;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.PaymentVerificationCodeRequest;
import com.soloproject.LegalPark.dto.request.verificationCode.VerifyPaymentCodeRequest;
import com.soloproject.LegalPark.entity.*;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.GenerateOtp;
import com.soloproject.LegalPark.repository.ParkingTransactionRepository;
import com.soloproject.LegalPark.repository.PaymentVerificationCodeRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import com.soloproject.LegalPark.service.verificationCode.VerificationCodeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceImplTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ParkingTransactionRepository parkingTransactionRepository;
    @Mock
    private PaymentVerificationCodeRepository paymentVerificationCodeRepository;
    @Mock
    private INotificationService notificationService;
    @Mock
    private ITemplateService templateService;

    @InjectMocks
    private VerificationCodeServiceImpl verificationCodeService;

    private MockedStatic<ResponseHandler> mockedResponseHandler;
    private MockedStatic<GenerateOtp> mockedGenerateOtp;

    private Users mockUser;
    private ParkingTransaction mockParkingTransaction;
    private PaymentVerificationCode mockPaymentVerificationCode;
    private String TEST_USER_ID = "user123";
    private String TEST_TRANSACTION_ID = "trans456";
    private String TEST_OTP_CODE = "123456";
    private String TEST_USER_EMAIL = "test@example.com";
    private String TEST_USER_ACCOUNT_NAME = "Test User";

    @BeforeEach
    void setUp() {
        mockedResponseHandler = mockStatic(ResponseHandler.class);
        mockedGenerateOtp = mockStatic(GenerateOtp.class);

        // Default mock for ResponseHandler's static methods
        // INI ADALAH BAGIAN YANG DISESUAIKAN UNTUK MENGHINDARI ERROR RESPONSEHANDLER() CANNOT BE APPLIED
        mockedResponseHandler.when(() -> ResponseHandler.generateResponseSuccess(any(HttpStatus.class), anyString(), any()))
                .thenAnswer(invocation -> {
                    // Membuat instance ResponseHandler menggunakan konstruktor default
                    ResponseHandler rh = new ResponseHandler();
                    rh.setCode(invocation.getArgument(0, HttpStatus.class).value());
                    rh.setStatus(invocation.getArgument(0, HttpStatus.class));
                    rh.setMessage(invocation.getArgument(1, String.class));
                    rh.setData(invocation.getArgument(2));
                    return ResponseEntity.status(invocation.getArgument(0, HttpStatus.class)).body(rh);
                });

        mockedResponseHandler.when(() -> ResponseHandler.generateResponseError(any(HttpStatus.class), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    // Membuat instance ResponseHandler menggunakan konstruktor default
                    ResponseHandler rh = new ResponseHandler();
                    rh.setCode(invocation.getArgument(0, HttpStatus.class).value());
                    rh.setStatus(invocation.getArgument(0, HttpStatus.class));
                    rh.setMessage(invocation.getArgument(2, String.class)); // Pesan error
                    rh.setError(invocation.getArgument(1, String.class));   // Objek error / kode error
                    return ResponseEntity.status(invocation.getArgument(0, HttpStatus.class)).body(rh);
                });

        // Setup common mock entities
        mockUser = new Users();
        mockUser.setId(TEST_USER_ID);
        mockUser.setEmail(TEST_USER_EMAIL);
        mockUser.setAccountName(TEST_USER_ACCOUNT_NAME);

        Merchant merchant = new Merchant();
        merchant.setId("merchant1");
        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setId("ps1");
        parkingSpot.setMerchant(merchant);
        Vehicle vehicle = new Vehicle();
        vehicle.setId("v1");

        mockParkingTransaction = new ParkingTransaction();
        mockParkingTransaction.setId(TEST_TRANSACTION_ID);
        mockParkingTransaction.setStatus(ParkingStatus.ACTIVE);
        mockParkingTransaction.setParkingSpot(parkingSpot);
        mockParkingTransaction.setVehicle(vehicle);

        mockPaymentVerificationCode = new PaymentVerificationCode();
        mockPaymentVerificationCode.setId("code1");
        mockPaymentVerificationCode.setUser(mockUser);
        mockPaymentVerificationCode.setCode(TEST_OTP_CODE);
        mockPaymentVerificationCode.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        mockPaymentVerificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // Not expired
        mockPaymentVerificationCode.setVerified(false);
        mockPaymentVerificationCode.setParkingTransaction(mockParkingTransaction); // Link to the transaction

        // Default behavior for OTP generation
        mockedGenerateOtp.when(GenerateOtp::generateRandomNumber).thenReturn(TEST_OTP_CODE);
        mockedGenerateOtp.when(GenerateOtp::getExpiryDate).thenReturn(LocalDateTime.now().plusMinutes(5));
    }

    @AfterEach
    void tearDown() {
        mockedResponseHandler.close();
        mockedGenerateOtp.close();
    }

    // --- Tests for generateAndSendPaymentVerificationCode ---

    @Test
    void generateAndSendPaymentVerificationCode_Success() {
        // Arrange
        // INI ADALAH BAGIAN YANG DISESUAIKAN UNTUK MENGHINDARI ERROR PAYMENTVERIFICATIONCODEREQUEST() CANNOT BE APPLIED
        PaymentVerificationCodeRequest request = new PaymentVerificationCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(parkingTransactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.of(mockParkingTransaction));
        when(paymentVerificationCodeRepository.save(any(PaymentVerificationCode.class))).thenReturn(mockPaymentVerificationCode);
        when(templateService.processEmailTemplate(eq("payment_otp_verification"), anyMap())).thenReturn("Email Body HTML");
        doNothing().when(notificationService).sendEmailNotification(any(EmailNotificationRequest.class));

        // Act
        ResponseEntity<Object> response = verificationCodeService.generateAndSendPaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Payment verification code sent.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findById(TEST_TRANSACTION_ID);
        verify(paymentVerificationCodeRepository).save(any(PaymentVerificationCode.class));
        verify(templateService).processEmailTemplate(eq("payment_otp_verification"), anyMap());

        ArgumentCaptor<EmailNotificationRequest> emailCaptor = ArgumentCaptor.forClass(EmailNotificationRequest.class);
        verify(notificationService).sendEmailNotification(emailCaptor.capture());
        EmailNotificationRequest capturedEmail = emailCaptor.getValue();
        assertEquals(TEST_USER_EMAIL, capturedEmail.getTo());
        assertEquals("LegalPark - Kode Verifikasi Pembayaran Anda", capturedEmail.getSubject());
        assertEquals("Email Body HTML", capturedEmail.getBody());

        mockedGenerateOtp.verify(GenerateOtp::generateRandomNumber);
        mockedGenerateOtp.verify(GenerateOtp::getExpiryDate);
    }

    @Test
    void generateAndSendPaymentVerificationCode_UserNotFound() {
        // Arrange
        PaymentVerificationCodeRequest request = new PaymentVerificationCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = verificationCodeService.generateAndSendPaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("User not found.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(parkingTransactionRepository, paymentVerificationCodeRepository, notificationService, templateService);
        mockedGenerateOtp.verifyNoInteractions();
    }

    @Test
    void generateAndSendPaymentVerificationCode_ParkingTransactionNotFound() {
        // Arrange
        PaymentVerificationCodeRequest request = new PaymentVerificationCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(parkingTransactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = verificationCodeService.generateAndSendPaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Active parking transaction not found or invalid for ID: " + TEST_TRANSACTION_ID, responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findById(TEST_TRANSACTION_ID);
        verifyNoInteractions(paymentVerificationCodeRepository, notificationService, templateService);
        mockedGenerateOtp.verifyNoInteractions();
    }

    @Test
    void generateAndSendPaymentVerificationCode_ParkingTransactionNotActive() {
        // Arrange
        PaymentVerificationCodeRequest request = new PaymentVerificationCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);
        mockParkingTransaction.setStatus(ParkingStatus.COMPLETED); // Set to non-active status

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(parkingTransactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.of(mockParkingTransaction));

        // Act
        ResponseEntity<Object> response = verificationCodeService.generateAndSendPaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Active parking transaction not found or invalid for ID: " + TEST_TRANSACTION_ID, responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findById(TEST_TRANSACTION_ID);
        verifyNoInteractions(paymentVerificationCodeRepository, notificationService, templateService);
        mockedGenerateOtp.verifyNoInteractions();
    }

    @Test
    void generateAndSendPaymentVerificationCode_EmailServiceFailure_ThrowsRuntimeException() {
        // Arrange
        PaymentVerificationCodeRequest request = new PaymentVerificationCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(parkingTransactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.of(mockParkingTransaction));
        when(paymentVerificationCodeRepository.save(any(PaymentVerificationCode.class))).thenReturn(mockPaymentVerificationCode);
        when(templateService.processEmailTemplate(eq("payment_otp_verification"), anyMap())).thenReturn("Email Body HTML");
        doThrow(new RuntimeException("Email sending failed")).when(notificationService).sendEmailNotification(any(EmailNotificationRequest.class));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                verificationCodeService.generateAndSendPaymentVerificationCode(request)
        );
        assertTrue(thrown.getMessage().contains("Failed to generate and send payment verification code"));
        assertTrue(thrown.getMessage().contains("Email sending failed"));

        verify(usersRepository).findById(TEST_USER_ID);
        verify(parkingTransactionRepository).findById(TEST_TRANSACTION_ID);
        verify(paymentVerificationCodeRepository).save(any(PaymentVerificationCode.class)); // Should still be saved before email fails
        verify(templateService).processEmailTemplate(eq("payment_otp_verification"), anyMap());
        verify(notificationService).sendEmailNotification(any(EmailNotificationRequest.class));
    }


    // --- Tests for validatePaymentVerificationCode ---

    @Test
    void validatePaymentVerificationCode_Success() {
        // Arrange
        // INI ADALAH BAGIAN YANG DISESUAIKAN UNTUK MENGHINDARI ERROR VERIFYPAYMENTCODEREQUEST() CANNOT BE APPLIED
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(paymentVerificationCodeRepository.findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE)))
                .thenReturn(Optional.of(mockPaymentVerificationCode));
        when(paymentVerificationCodeRepository.save(any(PaymentVerificationCode.class))).thenReturn(mockPaymentVerificationCode);

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Payment verification successful.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(paymentVerificationCodeRepository).findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE));
        verify(paymentVerificationCodeRepository).save(argThat(code -> code.isVerified())); // Ensure verified is set to true
        verifyNoInteractions(notificationService, templateService); // No email sent on success verification
    }

    @Test
    void validatePaymentVerificationCode_UserNotFound() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("User not found.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }

    @Test
    void validatePaymentVerificationCode_InvalidOrExpiredCode_NotFound() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode("wrongCode");
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(paymentVerificationCodeRepository.findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq("wrongCode")))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Invalid or expired verification code.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(paymentVerificationCodeRepository).findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq("wrongCode"));
        verifyNoMoreInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }

    @Test
    void validatePaymentVerificationCode_CodeExpired() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);
        mockPaymentVerificationCode.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Set expired

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(paymentVerificationCodeRepository.findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE)))
                .thenReturn(Optional.of(mockPaymentVerificationCode));

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Verification code has expired.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(paymentVerificationCodeRepository).findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE));
        verifyNoMoreInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }

    @Test
    void validatePaymentVerificationCode_TransactionIdMismatch() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId("anotherTransactionId"); // Mismatched ID

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(paymentVerificationCodeRepository.findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE)))
                .thenReturn(Optional.of(mockPaymentVerificationCode));

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Verification code is not valid for this transaction.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(paymentVerificationCodeRepository).findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE));
        verifyNoMoreInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }

    @Test
    void validatePaymentVerificationCode_CodeNotLinkedToAnyTransaction() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);
        mockPaymentVerificationCode.setParkingTransaction(null); // Code is not linked to any transaction

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(paymentVerificationCodeRepository.findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE)))
                .thenReturn(Optional.of(mockPaymentVerificationCode));

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Verification code is not valid for this transaction.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(paymentVerificationCodeRepository).findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE));
        verifyNoMoreInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }

    @Test
    void validatePaymentVerificationCode_AlreadyVerifiedCode() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);
        mockPaymentVerificationCode.setVerified(true); // Code is already verified

        when(usersRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        // findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc should return empty if already verified
        when(paymentVerificationCodeRepository.findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = verificationCodeService.validatePaymentVerificationCode(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ResponseHandler responseBody = (ResponseHandler) response.getBody();
        assertNotNull(responseBody);
        assertEquals("FAILED", responseBody.getError());
        assertEquals("Invalid or expired verification code.", responseBody.getMessage());

        verify(usersRepository).findById(TEST_USER_ID);
        verify(paymentVerificationCodeRepository).findTopByUserAndCodeAndIsVerifiedFalseOrderByExpiresAtDesc(eq(mockUser), eq(TEST_OTP_CODE));
        verifyNoMoreInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }

    @Test
    void validatePaymentVerificationCode_ExceptionThrown() {
        // Arrange
        VerifyPaymentCodeRequest request = new VerifyPaymentCodeRequest();
        request.setUserId(TEST_USER_ID);
        request.setCode(TEST_OTP_CODE);
        request.setParkingTransactionId(TEST_TRANSACTION_ID);

        when(usersRepository.findById(TEST_USER_ID)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                verificationCodeService.validatePaymentVerificationCode(request)
        );
        assertTrue(thrown.getMessage().contains("Failed to validate payment verification code: Database error"));

        verify(usersRepository).findById(TEST_USER_ID);
        verifyNoInteractions(paymentVerificationCodeRepository, notificationService, templateService);
    }
}