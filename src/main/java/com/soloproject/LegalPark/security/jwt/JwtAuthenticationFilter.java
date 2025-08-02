package com.soloproject.LegalPark.security.jwt;

import com.soloproject.LegalPark.repository.UsersRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsersRepository usersRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, UsersRepository usersRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.usersRepository = usersRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {

//                    var findEmail = usersRepository.findByEmail(username);
//
//                    if(findEmail.get() != null && findEmail.get().getAccountStatus() == AccountStatus.UNVERIFIED){
//                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                        response.setContentType("application/json");
//                        response.getWriter().write("{ \"code\": 403,\"status\": \"Forbidden\", \"message\": \"Your account is not verified. Please verify your account to proceed..\", }");
//                        return;
//                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{ \"code\": 400,\"status\": \" Bad Request \", \"message\": \"Token expired. Please login again.\", }");
            return;
        }  catch (MalformedJwtException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{ \"code\": 400,\"status\": \" Bad Request \",\"message\": \"Invalid token.\" }");
            return;
        } catch (SignatureException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{ \"code\": 400,\"status\": \" Bad Request \", \"message\": \"JWT signature mismatch: Invalid signature.\"  }");
            return;
        } catch (ClassCastException ex){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{ \"code\": 400,\"status\": \" Bad Request \", \"message\": \"Invalid value format in JWT.\" }");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
