package com.soloproject.LegalPark.config;

import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.repository.UsersRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
public class AppConfig {
    private final UsersRepository usersRepository;

    public AppConfig(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public UserDetailsService userDetailsService(){
//        return username -> usersRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        return username -> {
            Users user = usersRepository.findByEmail(username) // Cari user berdasarkan email (yang digunakan untuk login)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

            // clean code version
            Collection<GrantedAuthority> authorities =
                    (user.getRole() != null)
                            ? List.of(new SimpleGrantedAuthority(user.getRole().name()))
                            : Collections.emptyList();

            // PENTING: Buat objek UserDetails Spring Security dengan ID user sebagai username (principal name)
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), 
                    user.getPassword(),
                    authorities
//                    (user.getRole() != null)
//                    ? List.of(new SimpleGrantedAuthority(user.getRole().name())) // Ganti dengan peran contohnya seperti disamping bro -> (misal: List.of(new SimpleGrantedAuthority(user.getRole())))
//                            : Collections.emptyList()
            );
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LegalPark API Documentation")
                        .version("3.1.0")
                        .description("Dokumentasi API untuk aplikasi LegalPark"));
//                .servers(List.of(new Server().url("http://localhost:8080").description("Local Server")));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
}
