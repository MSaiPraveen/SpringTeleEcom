package com.example.SpringTeleEcom.config;

import com.example.SpringTeleEcom.security.JwtAuthenticationFilter;
import com.example.SpringTeleEcom.security.CustomOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    // Read allowed origins from env (comma-separated) with sensible defaults
    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrlEnv;

    @Value("${ADDITIONAL_ALLOWED_ORIGINS:}")
    private String additionalAllowedOriginsEnv;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // enable CORS using our corsConfigurationSource bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth APIs (login / register)
                        .requestMatchers("/api/auth/**").permitAll()

                        // OAuth2 endpoints (Google/GitHub redirect flows)
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // Public product browse endpoints
                        .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()

                        // Orders: placing / viewing require auth; admin control is via @PreAuthorize
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/orders/my").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Build allowed origins list from FRONTEND_URL and an optional extra list
        List<String> origins = new ArrayList<>();
        if (frontendUrlEnv != null && !frontendUrlEnv.isBlank()) {
            origins.add(frontendUrlEnv.trim());
        }
        if (additionalAllowedOriginsEnv != null && !additionalAllowedOriginsEnv.isBlank()) {
            origins.addAll(Arrays.stream(additionalAllowedOriginsEnv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }

        // For dev convenience include localhost ports if not already present
        if (origins.stream().noneMatch(o -> o.contains("localhost"))) {
            origins.addAll(Arrays.asList("http://localhost:5173", "http://localhost:5174"));
        }

        // Use allowedOriginPatterns to support dynamic subdomains and https origins if needed.
        // We still set explicit origins first (safer).
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedOriginPatterns(List.of("https://*.vercel.app", "https://*.onrender.com"));

        // Allow common methods and headers, and credentials (cookies)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
