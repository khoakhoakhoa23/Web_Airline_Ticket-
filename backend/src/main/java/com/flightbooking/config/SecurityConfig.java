package com.flightbooking.config;

import com.flightbooking.dto.ErrorResponse;
import com.flightbooking.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration for REST API + JWT Authentication
 * 
 * Features:
 * - CSRF disabled (stateless API)
 * - CORS enabled for React frontend
 * - JWT-based authentication
 * - Public endpoints: /api/users/register, /api/users/login, /api/flights/search
 * - Protected endpoints: All others require valid JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * CORS Configuration for React Frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (React dev servers)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5174"
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers (including Authorization)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expose Authorization header to frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * ✅ CRITICAL: AuthenticationEntryPoint to handle unauthenticated requests
     * 
     * Returns 401 UNAUTHORIZED instead of 404 when user is not authenticated
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, 
                               HttpServletResponse response,
                               AuthenticationException authException) throws IOException {
                
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                
                ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("UNAUTHORIZED")
                    .message(authException != null && authException.getMessage() != null 
                        ? authException.getMessage() 
                        : "User not authenticated. Please login to continue.")
                    .path(request.getRequestURI())
                    .build();
                
                response.getWriter().write(
                    "{\"timestamp\":\"" + errorResponse.getTimestamp() + "\"," +
                    "\"status\":" + errorResponse.getStatus() + "," +
                    "\"error\":\"" + errorResponse.getError() + "\"," +
                    "\"message\":\"" + errorResponse.getMessage() + "\"," +
                    "\"path\":\"" + errorResponse.getPath() + "\"}"
                );
            }
        };
    }
    
    /**
     * ✅ CRITICAL: AccessDeniedHandler to handle unauthorized requests
     * 
     * Returns 403 FORBIDDEN when user is authenticated but doesn't have permission
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, 
                HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message("You do not have permission to access this resource")
                .path(request.getRequestURI())
                .build();
            
            response.getWriter().write(
                "{\"timestamp\":\"" + errorResponse.getTimestamp() + "\"," +
                "\"status\":" + errorResponse.getStatus() + "," +
                "\"error\":\"" + errorResponse.getError() + "\"," +
                "\"message\":\"" + errorResponse.getMessage() + "\"," +
                "\"path\":\"" + errorResponse.getPath() + "\"}"
            );
        };
    }
    
    /**
     * Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless JWT API)
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS with custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session (JWT-based, no server-side sessions)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // ✅ Public endpoints (no authentication required)
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/flights/search",
                    "/api/flights",
                    "/api/flights/*",
                    "/api/payments/webhook/**"  // Webhook endpoints must be public
                ).permitAll()
                
                // ✅ Admin endpoints (require ADMIN role)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // ✅ All other endpoints require JWT authentication
                .anyRequest().authenticated()
            )
            
            // ✅ CRITICAL: Set AuthenticationEntryPoint to return 401 for unauthenticated requests
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            
            // Add JWT filter before Spring Security's authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

