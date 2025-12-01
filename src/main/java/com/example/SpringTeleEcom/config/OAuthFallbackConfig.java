package com.example.SpringTeleEcom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuthFallbackConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // No-op repository keeps SecurityConfig wiring intact when no OAuth providers are configured yet
        return new ClientRegistrationRepository() {
            @Override
            public ClientRegistration findByRegistrationId(String registrationId) {
                return null;
            }
        };
    }
}
