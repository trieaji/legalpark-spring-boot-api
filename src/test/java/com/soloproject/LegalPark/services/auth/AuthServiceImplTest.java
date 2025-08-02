package com.soloproject.LegalPark.services.auth;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.dto.request.users.LoginRequest;
import com.soloproject.LegalPark.dto.request.users.RegisterRequest;
import com.soloproject.LegalPark.dto.response.users.RegisterResponse;
import com.soloproject.LegalPark.dto.response.users.SignResponse;
import com.soloproject.LegalPark.entity.AccountStatus;
import com.soloproject.LegalPark.entity.LogVerification;
import com.soloproject.LegalPark.entity.Role;
import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.InfoAccount;
import com.soloproject.LegalPark.repository.LogVerificationRepository;
import com.soloproject.LegalPark.repository.UsersRepository;
import com.soloproject.LegalPark.security.jwt.JwtService;
import com.soloproject.LegalPark.service.auth.AuthServiceImpl;
import com.soloproject.LegalPark.service.notification.INotificationService;
import com.soloproject.LegalPark.service.template.ITemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper; // Karena diinisialisasi langsung, kita bisa mock atau instansiasi langsung di test
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mengaktifkan Mockito untuk JUnit 5
public class AuthServiceImplTest {

    @Mock // Mocking semua dependensi
    private UsersRepository usersRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private InfoAccount infoAccount; // Jika ini tidak digunakan, bisa dihapus atau diabaikan
    @Mock
    private LogVerificationRepository logVerificationRepository;
    @Mock
    private INotificationService notificationService;
    @Mock
    private ITemplateService templateService;

    @InjectMocks // Menginjeksikan mock ke instance AuthServiceImpl yang sebenarnya
    private AuthServiceImpl authService;

    // ModelMapper diinisialisasi langsung di service, jadi kita bisa mock atau instansiasi di sini
    // Untuk unit test, lebih baik instansiasi ModelMapper asli jika tidak ada konfigurasi khusus
    private ModelMapper modelMapper;

    @BeforeEach // Metode ini akan dijalankan sebelum setiap test
    void setUp() {
        // Inisialisasi ModelMapper asli
        modelMapper = new ModelMapper();
        // Set ModelMapper ke authService (jika tidak di-inject via constructor)
        // Jika ModelMapper di-inject via @Autowired, maka @Mock ModelMapper di atas akan bekerja
        // Karena Anda menginisialisasi langsung, kita perlu cara untuk memasukkannya ke service untuk test
        // Untuk kasus ini, karena ModelMapper adalah field final di AuthServiceImpl, kita perlu sedikit trik
        // atau mengubah AuthServiceImpl agar ModelMapper bisa di-inject atau disetel.
        // Untuk tujuan test, kita bisa menggunakan Reflection atau mengubah service agar ModelMapper bisa di-set.
        // Cara paling sederhana untuk test adalah mengubah service agar ModelMapper di-inject.
        // Jika tidak, kita bisa menganggap ModelMapper sebagai bagian dari "unit" yang diuji.

        // Jika Anda ingin menguji dengan ModelMapper yang di-mock:
        // when(modelMapper.map(any(), any())).thenReturn(new Users()); // Contoh mock map
    }


    // ====================================================================================================
    // TEST UNTUK METODE LOGIN
    // ====================================================================================================

    @Test
    void login_Success() {
        // 1. Persiapan Data Mock
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        Users mockUser = new Users();
        mockUser.setId(UUID.randomUUID().toString());
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(Role.USER);
        mockUser.setAccountName("Test User");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.isAuthenticated()).thenReturn(true); // Penting
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser); // Jika principal adalah Users object
        when(mockAuthentication.getName()).thenReturn(mockUser.getId()); // Jika principal name adalah ID

        // 2. Mock Perilaku Dependensi
        // Mock AuthenticationManager.authenticate()
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // Mock usersRepository.findByEmail()
        when(usersRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(mockUser));

        // Mock jwtService.generateToken()
        when(jwtService.generateToken(any(Users.class)))
                .thenReturn("mockAccessToken");

        // Mock jwtService.generateRefreshToken()
        when(jwtService.generateRefreshToken(mockUser.getId()))
                .thenReturn("mockRefreshToken");

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = authService.login(loginRequest);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        // Verifikasi ResponseHandler digunakan dengan benar
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        // Verifikasi SignResponse di dalamnya
        assertTrue(responseHandler.getData() instanceof SignResponse);
        SignResponse signResponse = (SignResponse) responseHandler.getData();
        assertEquals(mockUser.getEmail(), signResponse.getEmail());
        assertEquals(mockUser.getRole().name(), signResponse.getRole());
        assertEquals("mockAccessToken", signResponse.getToken());
        assertEquals("mockRefreshToken", signResponse.getRefreshToken());

        // Verifikasi bahwa dependensi dipanggil dengan benar
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usersRepository).findByEmail(loginRequest.getEmail());
        verify(jwtService).generateToken(mockUser);
        verify(jwtService).generateRefreshToken(mockUser.getId());
    }

    // Tambahkan test case untuk skenario login gagal (misal: UsernameNotFoundException, BadCredentialsException)
    // Anda perlu meng-mock authenticationManager.authenticate() untuk melempar eksepsi tersebut.
    // Contoh:
    // @Test
    // void login_InvalidCredentials_ReturnsUnauthorized() {
    //     LoginRequest loginRequest = new LoginRequest("wrong@example.com", "wrongpass");
    //     when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
    //             .thenThrow(new BadCredentialsException("Bad credentials"));
    //
    //     ResponseEntity<Object> responseEntity = authService.login(loginRequest);
    //
    //     assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    //     // Verifikasi pesan error, dll.
    // }


    // ====================================================================================================
    // TEST UNTUK METODE REGISTER
    // ====================================================================================================

    @Test
    void register_Success() {
        // 1. Persiapan Data Mock
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setAccountName("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPhoneNumber("08123456789");
        registerRequest.setPassword("securepass");

        Users mockSavedUser = new Users();
        mockSavedUser.setId(UUID.randomUUID().toString());
        mockSavedUser.setEmail(registerRequest.getEmail());
        mockSavedUser.setAccountName(registerRequest.getAccountName());
        mockSavedUser.setRole(Role.USER);
        mockSavedUser.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        mockSavedUser.setBalance(new BigDecimal("100000.00"));
        mockSavedUser.setPassword("encodedSecurePass"); // Setelah di-encode

        LogVerification mockLogVerification = new LogVerification();
        mockLogVerification.setCode("123456");
        mockLogVerification.setUser(mockSavedUser);
        mockLogVerification.setExpired(LocalDateTime.now().plusDays(1));
        mockLogVerification.setVerify(false);

        // 2. Mock Perilaku Dependensi
        // Mock usersRepository.findByEmail() dan findByPhoneNumber() agar tidak ada konflik
        when(usersRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());


        // Mock passwordEncoder.encode()
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedSecurePass");

        // Mock usersRepository.save()
        when(usersRepository.save(any(Users.class))).thenReturn(mockSavedUser);

        // Mock logVerificationRepository.save()
        when(logVerificationRepository.save(any(LogVerification.class))).thenReturn(mockLogVerification);

        // Mock GenerateOtp (karena static, kita perlu PowerMockito atau mock secara manual jika tidak pakai PowerMockito)
        // Untuk unit test sederhana, kita bisa menganggap GenerateOtp sebagai utility yang sudah diuji
        // Atau, jika GenerateOtp tidak static, kita bisa mock. Karena static, kita akan asumsikan perilakunya.
        // Jika Anda menggunakan PowerMockito, ini akan terlihat seperti:
        // Mockito.mockStatic(GenerateOtp.class);
        // when(GenerateOtp.generateRandomNumber()).thenReturn("123456");
        // when(GenerateOtp.getExpiryDate()).thenReturn(LocalDateTime.now().plusDays(1));

        // Mock templateService.processEmailTemplate()
        when(templateService.processEmailTemplate(eq("email_verification"), anyMap()))
                .thenReturn("<html>Email Content</html>");

        // Mock notificationService.sendEmailNotification()
        when(notificationService.sendEmailNotification(any(EmailNotificationRequest.class)))
                .thenReturn(ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Email sent", null));

        // Mock jwtService.generateToken()
        when(jwtService.generateToken(any(Users.class))).thenReturn("mockRegisterToken");

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = authService.register(registerRequest);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("success", responseHandler.getMessage());
        assertNotNull(responseHandler.getData());

        assertTrue(responseHandler.getData() instanceof RegisterResponse);
        RegisterResponse registerResponse = (RegisterResponse) responseHandler.getData();
        assertEquals(registerRequest.getEmail(), registerResponse.getEmail());
        assertEquals("mockRegisterToken", registerResponse.getToken());

        // Verifikasi bahwa dependensi dipanggil dengan benar
        verify(usersRepository).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(usersRepository).save(any(Users.class));
        verify(logVerificationRepository).save(any(LogVerification.class));
        verify(templateService).processEmailTemplate(eq("email_verification"), anyMap());
        verify(notificationService).sendEmailNotification(any(EmailNotificationRequest.class));
        verify(jwtService).generateToken(any(Users.class));
    }

    // Tambahkan test case untuk skenario register gagal
    @Test
    void register_EmailAlreadyExists_ReturnsConflict() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@example.com");

        // Mock usersRepository.findByEmail() untuk mengembalikan user yang sudah ada
        when(usersRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new Users()));

        ResponseEntity<Object> responseEntity = authService.register(registerRequest);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        // Verifikasi pesan error, dll.
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Email is already registered.", responseHandler.getMessage());

        // Pastikan save tidak dipanggil
        verify(usersRepository, never()).save(any(Users.class));
    }

    @Test
    void register_PhoneNumberAlreadyExists_ReturnsConflict() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com"); // Email baru
        registerRequest.setPhoneNumber("08123456789"); // Nomor telepon sudah ada

        // Mock usersRepository.findByEmail() agar tidak ada konflik email
        when(usersRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        // Mock usersRepository.findByPhoneNumber() untuk mengembalikan user yang sudah ada

        ResponseEntity<Object> responseEntity = authService.register(registerRequest);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("Phone number is already registered.", responseHandler.getMessage());

        verify(usersRepository, never()).save(any(Users.class));
    }


    // ====================================================================================================
    // TEST UNTUK METODE LOGOUT
    // ====================================================================================================

    @Test
    void logout_Success() {
        // 1. Persiapan Data Mock
        String userEmail = "test@example.com";
        Users mockUser = new Users();
        mockUser.setId(UUID.randomUUID().toString()); // Penting: ID adalah UUID
        mockUser.setEmail(userEmail);
        mockUser.setBalance(new BigDecimal("50000.00")); // Saldo awal yang akan direset

        // 2. Mock Perilaku Dependensi
        // Mock usersRepository.findByEmail()
        when(usersRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        // Mock usersRepository.save()
        when(usersRepository.save(any(Users.class))).thenReturn(mockUser); // Mengembalikan user yang sama setelah di-save

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = authService.logout(userEmail);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseHandler.getCode());
        assertEquals("Logout successful. Your virtual balance has been reset to 100k.", responseHandler.getMessage());

        // Verifikasi bahwa saldo di-reset
        verify(usersRepository).save(argThat(user -> user.getBalance().compareTo(new BigDecimal("100000.00")) == 0));
        verify(usersRepository).findByEmail(userEmail);
    }

    @Test
    void logout_UserNotFound_ReturnsNotFound() {
        // 1. Persiapan Data Mock
        String userEmail = "nonexistent@example.com";

        // 2. Mock Perilaku Dependensi
        // Mock usersRepository.findByEmail() untuk mengembalikan empty Optional
        when(usersRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // 3. Panggil Metode yang Diuji
        ResponseEntity<Object> responseEntity = authService.logout(userEmail);

        // 4. Verifikasi Hasil
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ResponseHandler);
        ResponseHandler responseHandler = (ResponseHandler) responseEntity.getBody();
        assertEquals("User not found.", responseHandler.getMessage());

        // Verifikasi bahwa save tidak dipanggil
        verify(usersRepository, never()).save(any(Users.class));
    }
}