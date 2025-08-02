package com.soloproject.LegalPark.security;

import com.soloproject.LegalPark.entity.Role;
import com.soloproject.LegalPark.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }

    private final String auth = "/api/v1/auth/**";
    private final String user = "/api/v1/user/**";
    private final String admin = "/api/v1/admin/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Swagger UI access
                                .requestMatchers(
                                        "/api-docs/**",
                                        "/api-docs",
                                        "/v3/api-documentation/**",
                                        "/v3/api-documentation",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/configuration/ui",
                                        "/configuration/security"
                                ).permitAll()
                        // Hanya buka akses untuk register, login, verification
                        .requestMatchers(auth).permitAll()

                        // Logout harus authenticated
                        .requestMatchers("/api/v1/auth/logout").authenticated()

                        // Public (contoh endpoint tambahan yang tidak butuh login)
                        .requestMatchers("/api/v1/merchants/**").permitAll()
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // User area
                        .requestMatchers(user).hasAnyAuthority(Role.USER.name(),Role.ADMIN.name())

                        // Admin area
                        .requestMatchers(admin).hasAuthority(Role.ADMIN.name())
                        
                        // Lainnya wajib authenticated
                        .anyRequest().authenticated()
                )
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .accessDeniedHandler(customAccessDeniedException)
//                        .authenticationEntryPoint(customAuthenticationEntryPoint)
//                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .apply(new CorsConfigurer<>());

        return http.build();
    }
}
