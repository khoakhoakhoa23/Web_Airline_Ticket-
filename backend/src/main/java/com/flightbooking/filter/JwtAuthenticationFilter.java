package com.flightbooking.filter;

import com.flightbooking.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * 
 * ✅ CRITICAL: This filter extracts JWT token from Authorization header,
 * validates it, extracts userId and role, and sets Authentication in SecurityContext.
 * 
 * Flow:
 * 1. Extract token from "Authorization: Bearer <token>" header
 * 2. Validate token (signature, expiration)
 * 3. Extract userId (from subject claim) and role
 * 4. Create UsernamePasswordAuthenticationToken with userId as principal
 * 5. Set Authentication in SecurityContext
 * 
 * ⚠️ IMPORTANT: 
 * - principal = userId (String UUID)
 * - This allows auth.getName() to return userId in controllers
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // ✅ Clear SecurityContext first to avoid stale authentication
        SecurityContextHolder.clearContext();
        
        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        
        logger.info("🔍 JWT Filter processing request: {} {}", request.getMethod(), requestURI);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("✅ Authorization header found. Token length: {}", token.length());
            
            try {
                // ✅ Step 1: Validate token signature
                logger.info("🔍 Step 1: Validating token signature...");
                boolean isValid = jwtUtil.validateToken(token);
                logger.info("   Token signature valid: {}", isValid);
                
                if (!isValid) {
                    logger.error("❌ Token signature invalid. URI: {}", requestURI);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // ✅ Step 2: Check token expiration
                logger.info("🔍 Step 2: Checking token expiration...");
                boolean isExpired = jwtUtil.isTokenExpired(token);
                logger.info("   Token expired: {}", isExpired);
                
                if (isExpired) {
                    logger.error("❌ Token expired. URI: {}", requestURI);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // ✅ Step 3: Extract userId from token subject claim
                logger.info("🔍 Step 3: Extracting userId from token...");
                String userId = jwtUtil.extractUserId(token);
                logger.info("   ✅ Extracted userId: {}", userId);
                
                if (userId == null || userId.trim().isEmpty()) {
                    logger.error("❌ CRITICAL: userId is null or empty after extraction!");
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // ✅ Step 4: Extract role and email
                logger.info("🔍 Step 4: Extracting role and email...");
                String role = jwtUtil.extractRole(token);
                String email = jwtUtil.extractEmail(token);
                logger.info("   ✅ Extracted role: {}, email: {}", role, email);
                
                // ✅ Step 5: Create Authentication with userId as principal
                logger.info("🔍 Step 5: Creating Authentication object...");
                logger.info("   Principal (userId): {}", userId);
                logger.info("   Role: {}", role);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId,  // ✅ CRITICAL: Principal = userId (String UUID)
                        null,    // Credentials = null
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                
                // ✅ Step 6: Set Authentication in SecurityContext
                logger.info("🔍 Step 6: Setting Authentication in SecurityContext...");
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // ✅ Step 7: Verify Authentication was set correctly
                Authentication verifyAuth = SecurityContextHolder.getContext().getAuthentication();
                if (verifyAuth != null && verifyAuth.isAuthenticated()) {
                    String verifyUserId = verifyAuth.getName();
                    logger.info("✅ SUCCESS: Authentication set in SecurityContext");
                    logger.info("   Principal (userId): {}", verifyUserId);
                    logger.info("   Is authenticated: {}", verifyAuth.isAuthenticated());
                    logger.info("   Authorities: {}", verifyAuth.getAuthorities());
                } else {
                    logger.error("❌ CRITICAL: Authentication was NOT set correctly!");
                }
                
            } catch (JwtException e) {
                logger.error("❌ JWT Exception on {}: {}", requestURI, e.getMessage(), e);
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                logger.error("❌ Unexpected error in JWT filter on {}: {}", requestURI, e.getMessage(), e);
                SecurityContextHolder.clearContext();
            }
        } else {
            // ✅ No Authorization header - request is unauthenticated
            logger.info("⚠️ No Authorization header found. URI: {} - Request will be rejected as unauthenticated", requestURI);
        }
        
        // ✅ Continue filter chain
        filterChain.doFilter(request, response);
    }
}

