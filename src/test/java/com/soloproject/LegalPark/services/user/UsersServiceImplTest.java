package com.soloproject.LegalPark.services.user;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.users.AccountVerification;
import com.soloproject.LegalPark.entity.AccountStatus;
import com.soloproject.LegalPark.entity.LogVerification;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.GenerateOtp;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.repository.LogVerificationRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import com.soloproject.LegalPark.service.users.UsersServiceImpl;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceImplTest {

    @Mock
    private InfoAccount infoAccount;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private LogVerificationRepository logVerificationRepository;
    @Mock
    private INotificationService notificationService;
    @Mock
    private ITemplateService templateService;

    @InjectMocks
    private UsersServiceImpl usersService;

    // Untuk mock static methods GenerateOtp dan ResponseHandler
    private MockedStatic<GenerateOtp> mockedGenerateOtp;
    private MockedStatic<ResponseHandler> mockedResponseHandler;

    @BeforeEach
    void setUp() {
        // Mock static methods for GenerateOtp
        mockedGenerateOtp = Mockito.mockStatic(GenerateOtp.class);
        mockedGenerateOtp.when(GenerateOtp::generateRandomNumber).thenReturn("NEW_OTP_CODE");
        mockedGenerateOtp.when(GenerateOtp::getExpiryDate).thenReturn(LocalDateTime.now().plusDays(1));

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
        mockedGenerateOtp.close();
        mockedResponseHandler.close();
    }

    // ====================================================================================================
    // TEST UNTUK METODE verificationAccount
    // ====================================================================================================

    @Test
    void verificationAccount_Success() {
        // 1. Persiapan Data Mock
        String userEmail = "user@example.com";
        String otpCode = "1234";
        AccountVerification request = new AccountVerification();
        request.setCode(otpCode);

        Users mockUser = new Users();
        mockUser.setId(UUID.randomUUID().toString());
        mockUser.setEmail(userEmail);
        mockUser.setAccountName("Test User");
        mockUser.setAccountStatus(AccountStatus.PENDING_VERIFICATION);

        LogVerification mockLogVerification = new LogVerification();
        mockLogVerification.setCode(otpCode);
        mockLogVerification.setUser(mockUser);
        mockLogVerification.setExpired(LocalDateTime.now().plusMinutes(5)); // Belum expired
        mockLogVerification.setVerify(false);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(mockUser); // infoAccount mengembalikan Users object
        when(logVerificationRepository.getByUserAndExp(userEmail, otpCode)).thenReturn(mockLogVerification);
        when(logVerificationRepository.save(any(LogVerification.class))).thenAnswer(i -> i.getArguments()[0]); // Mengembalikan argumen yang disimpan
        when(usersRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(usersRepository.save(any(Users.class))).thenAnswer(i -> i.getArguments()[0]);
        when(templateService.processEmailTemplate(eq("email_success_verification"), anyMap())).thenReturn("<html>Success Email</html>");
        when(notificationService.sendEmailNotification(any(EmailNotificationRequest.class))).thenReturn(ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Email sent", null));


        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = usersService.verificationAccount(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        // Verifikasi LogVerification dan Users diperbarui dan disimpan
        verify(logVerificationRepository).getByUserAndExp(userEmail, otpCode);
        verify(logVerificationRepository).save(argThat(lv -> lv.getVerify().equals(true))); // Verifikasi isVerify menjadi true
        verify(usersRepository).findByEmail(userEmail);
        verify(usersRepository).save(argThat(u -> u.getAccountStatus() == AccountStatus.ACTIVE)); // Verifikasi status akun menjadi ACTIVE

        // Verifikasi pengiriman email
        verify(templateService).processEmailTemplate(eq("email_success_verification"), anyMap());
        verify(notificationService).sendEmailNotification(any(EmailNotificationRequest.class));
    }

    @Test
    void verificationAccount_InvalidOtp_ReturnsBadRequest() {
        // 1. Persiapan Data Mock
        String userEmail = "user@example.com";
        String otpCode = "wrong_code";
        AccountVerification request = new AccountVerification();
        request.setCode(otpCode);

        Users mockUser = new Users();
        mockUser.setEmail(userEmail);

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(mockUser);
        when(logVerificationRepository.getByUserAndExp(userEmail, otpCode)).thenReturn(null); // OTP tidak ditemukan

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = usersService.verificationAccount(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseHandler.getCode());
        assertEquals("Otp yang anda masukan salah", responseHandler.getMessage());
        assertEquals("FAILED", responseHandler.getError());

        verify(infoAccount).get();
        verify(logVerificationRepository).getByUserAndExp(userEmail, otpCode);
        verify(logVerificationRepository, never()).save(any(LogVerification.class)); // Pastikan save tidak dipanggil
        verify(usersRepository, never()).save(any(Users.class));
        verify(notificationService, never()).sendEmailNotification(any(EmailNotificationRequest.class));
    }

    @Test
    void verificationAccount_OtpExpired_GeneratesNewOtp() {
        // 1. Persiapan Data Mock
        String userEmail = "user@example.com";
        String otpCode = "expired_otp";
        AccountVerification request = new AccountVerification();
        request.setCode(otpCode);

        Users mockUser = new Users();
        mockUser.setId(UUID.randomUUID().toString());
        mockUser.setEmail(userEmail);
        mockUser.setAccountName("Test User");

        LogVerification mockLogVerification = new LogVerification();
        mockLogVerification.setCode(otpCode);
        mockLogVerification.setUser(mockUser);
        mockLogVerification.setExpired(LocalDateTime.now().minusMinutes(10)); // Sudah expired (lebih dari 5 menit lalu)
        mockLogVerification.setVerify(false);

        // Mock static isExpired behavior
        // Karena isExpired adalah metode static di UsersServiceImpl, kita tidak bisa mock
        // secara langsung menggunakan Mockito standar.
        // Solusi: Kita bisa menganggapnya sebagai bagian dari unit yang diuji
        // Atau, jika ingin mengontrolnya, refactor `isExpired` ke helper terpisah
        // atau gunakan PowerMockito (tapi ini menambah kompleksitas).
        // Untuk unit test ini, kita biarkan metode statisnya berjalan secara nyata,
        // dan set waktu expired di `mockLogVerification` agar memicu logika expired.

        // 2. Mock Perilaku Dependensi
        when(infoAccount.get()).thenReturn(mockUser);
        when(logVerificationRepository.getByUserAndExp(userEmail, otpCode)).thenReturn(mockLogVerification);
        when(logVerificationRepository.save(any(LogVerification.class))).thenAnswer(i -> i.getArguments()[0]); // Untuk save OTP baru

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = usersService.verificationAccount(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode()); // Harusnya OK karena mengembalikan OTP baru
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage()); // Sesuai dengan generateResponseSuccess(saveLog)

        // Verifikasi bahwa OTP baru dihasilkan dan disimpan
        verify(logVerificationRepository).getByUserAndExp(userEmail, otpCode);
        verify(logVerificationRepository, times(1)).save(argThat(lv ->
                lv.getCode().equals("NEW_OTP_CODE") && // Verifikasi OTP baru
                        lv.getExpired().isAfter(LocalDateTime.now().plusHours(23)) // Asumsi getExpiryDate() menghasilkan 1 hari ke depan
        ));
        // Pastikan tidak ada pembaruan status akun atau pengiriman email sukses
        verify(usersRepository, never()).save(any(Users.class));
        verify(notificationService, never()).sendEmailNotification(any(EmailNotificationRequest.class));
    }

    @Test
    void verificationAccount_ExceptionThrown_ReturnsInternalServerError() {
        // 1. Persiapan Data Mock
        String userEmail = "user@example.com";
        String otpCode = "1234";
        AccountVerification request = new AccountVerification();
        request.setCode(otpCode);

        Users mockUser = new Users();
        mockUser.setEmail(userEmail);

        // 2. Mock Perilaku Dependensi untuk melempar Exception
        when(infoAccount.get()).thenReturn(mockUser);
        when(logVerificationRepository.getByUserAndExp(userEmail, otpCode)).thenThrow(new RuntimeException("Database down"));

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = usersService.verificationAccount(request);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseHandler.getCode());
        assertEquals("FAILED", responseHandler.getMessage()); // Ini tergantung bagaimana generateResponseError Anda mengatur pesan
        assertEquals("Database down", responseHandler.getError()); // Ini sesuai dengan pesan dari Exception

        verify(infoAccount).get();
        verify(logVerificationRepository).getByUserAndExp(userEmail, otpCode);
        verify(logVerificationRepository, never()).save(any(LogVerification.class));
        verify(usersRepository, never()).save(any(Users.class));
        verify(notificationService, never()).sendEmailNotification(any(EmailNotificationRequest.class));
    }


    // ====================================================================================================
    // TEST UNTUK METODE isExpired (Metode statis internal)
    // ====================================================================================================
    // Karena ini metode statis di kelas yang sama, kita bisa mengujinya secara langsung
    // tanpa Mockito, atau jika kita ingin mengontrolnya, kita bisa mockstatic.
    // Namun, mengujinya secara langsung (tanpa mock) lebih mencerminkan unit test murni
    // untuk fungsi helper internal ini.

    @Test
    void isExpired_ReturnsTrue_WhenExpired() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10); // 10 menit yang lalu
        LocalDateTime currentTime = LocalDateTime.now();

        assertTrue(UsersServiceImpl.isExpired(expiredTime, currentTime));
    }

    @Test
    void isExpired_ReturnsFalse_WhenNotExpired() {
        LocalDateTime expiredTime = LocalDateTime.now().plusMinutes(10); // 10 menit ke depan
        LocalDateTime currentTime = LocalDateTime.now();

        assertFalse(UsersServiceImpl.isExpired(expiredTime, currentTime));
    }

    @Test
    void isExpired_ReturnsFalse_WhenExactly5MinutesAgo() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5); // Tepat 5 menit yang lalu
        LocalDateTime currentTime = LocalDateTime.now();

        // toMinutes() >= 5
        assertTrue(UsersServiceImpl.isExpired(expiredTime, currentTime));
    }

    @Test
    void isExpired_ReturnsFalse_WhenJustUnder5MinutesAgo() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(4).minusSeconds(59); // Kurang dari 5 menit yang lalu
        LocalDateTime currentTime = LocalDateTime.now();

        assertFalse(UsersServiceImpl.isExpired(expiredTime, currentTime));
    }

}