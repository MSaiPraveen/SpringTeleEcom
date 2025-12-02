package com.example.SpringTeleEcom.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuth2ClientConfig {

    private final OAuth2ClientProperties oAuth2ClientProperties;

    public OAuth2ClientConfig(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // Get all client registrations from application properties
        oAuth2ClientProperties.getRegistration().forEach((key, registration) -> {
            try {
                // Get the provider configuration if it exists
                OAuth2ClientProperties.Provider provider = registration.getProvider() != null ?
                    oAuth2ClientProperties.getProvider().get(registration.getProvider()) : null;
                
                ClientRegistration clientRegistration = buildClientRegistration(key, registration, provider);
                
                if (clientRegistration != null) {
                    registrations.add(clientRegistration);
                    System.out.println("✅ " + key.toUpperCase() + " OAuth2 registered with client ID: " + registration.getClientId());
                } else {
                    System.out.println("⚠️ " + key.toUpperCase() + " OAuth2 NOT registered (invalid configuration)");
                }
            } catch (Exception e) {
                System.err.println("Error configuring OAuth2 client " + key + ": " + e.getMessage());
            }
        });

        if (registrations.isEmpty()) {
            System.out.println("⚠️ NO OAuth2 providers registered - OAuth2 login disabled");
            return registrationId -> null;
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration buildClientRegistration(String registrationId, 
            OAuth2ClientProperties.Registration registration,
            OAuth2ClientProperties.Provider provider) {
        
        try {
            ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(registrationId)
                .clientId(registration.getClientId())
                .clientSecret(registration.getClientSecret())
                .scope(registration.getScope())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(registration.getRedirectUri())
                .clientName(registration.getClientName() != null ? 
                    registration.getClientName() : 
                    registrationId);
            
            // Set authorization URI
            if (provider != null && provider.getAuthorizationUri() != null) {
                builder.authorizationUri(provider.getAuthorizationUri());
            } else {
                builder.authorizationUri(getDefaultAuthorizationUri(registrationId));
            }
            
            // Set token URI
            if (provider != null && provider.getTokenUri() != null) {
                builder.tokenUri(provider.getTokenUri());
            } else {
                builder.tokenUri(getDefaultTokenUri(registrationId));
            }
            
            // Set user info URI if available
            if (provider != null && provider.getUserInfoUri() != null) {
                builder.userInfoUri(provider.getUserInfoUri());
            } else {
                String userInfoUri = getDefaultUserInfoUri(registrationId);
                if (userInfoUri != null) {
                    builder.userInfoUri(userInfoUri);
                }
            }
            
            // Set user name attribute name
            builder.userNameAttributeName(getDefaultUserNameAttributeName(registrationId));
            
            // Set jwkSetUri for Google
            if ("google".equalsIgnoreCase(registrationId)) {
                builder.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs");
            }
            
            return builder.build();
        } catch (Exception e) {
            System.err.println("Error building client registration for " + registrationId + ": " + e.getMessage());
            return null;
        }
    }

    private String getDefaultAuthorizationUri(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "https://accounts.google.com/o/oauth2/v2/auth";
            case "github" -> "https://github.com/login/oauth/authorize";
            default -> throw new IllegalArgumentException("No default authorization URI for " + registrationId);
        };
    }

    private String getDefaultTokenUri(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "https://www.googleapis.com/oauth2/v4/token";
            case "github" -> "https://github.com/login/oauth/access_token";
            default -> throw new IllegalArgumentException("No default token URI for " + registrationId);
        };
    }

    private String getDefaultUserInfoUri(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "https://www.googleapis.com/oauth2/v3/userinfo";
            case "github" -> "https://api.github.com/user";
            default -> throw new IllegalArgumentException("No default user info URI for " + registrationId);
        };
    }

    private String getDefaultUserNameAttributeName(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "sub";
            case "github" -> "id";
            default -> "name";
        };
    }
}
