package com.example.SpringTeleEcom.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String header = request.getHeader("Authorization");

        System.out.println("🔐 JWT Filter - Path: " + path + ", Auth Header: " + (header != null ? "Present" : "Missing"));

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("⚠️  No Bearer token found, continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            String username = jwtService.getUsernameFromToken(token);

            System.out.println("👤 Extracted username from token: " + username);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Authentication successful for user: " + username);
                } else {
                    System.out.println("❌ Token validation failed for user: " + username);
                }
            }
        } catch (Exception e) {
            // Log the JWT error but don't block the request
            // This allows public endpoints to work even with invalid tokens
            System.err.println("❌ JWT validation error: " + e.getMessage());
            logger.warn("JWT validation failed: " + e.getMessage());
            // Clear any partial authentication that might have been set
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

