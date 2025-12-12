package com.example.SpringTeleEcom.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Configuration
public class OAuth2ClientConfig {

    private final OAuth2ClientProperties oAuth2ClientProperties;

    public OAuth2ClientConfig(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        Map<String, OAuth2ClientProperties.Registration> regMap = oAuth2ClientProperties.getRegistration();
        if (regMap != null) {
            regMap.forEach((registrationId, registration) -> {
                try {
                    OAuth2ClientProperties.Provider provider = null;

                    if (registration.getProvider() != null &&
                            oAuth2ClientProperties.getProvider() != null) {
                        provider = oAuth2ClientProperties.getProvider().get(registration.getProvider());
                    }

                    ClientRegistration clientRegistration =
                            buildClientRegistration(registrationId, registration, provider);

                    if (clientRegistration != null) {
                        registrations.add(clientRegistration);
                        System.out.println("✅ " + registrationId.toUpperCase() +
                                " OAuth2 registered with client ID: " + mask(registration.getClientId()));
                    } else {
                        System.out.println("⚠️ " + registrationId.toUpperCase() +
                                " OAuth2 NOT registered (invalid configuration)");
                    }

                } catch (Exception e) {
                    System.err.println("Error configuring OAuth2 client '" +
                            registrationId + "': " + e.getMessage());
                }
            });
        }

        // IMPORTANT: InMemoryClientRegistrationRepository refuses empty lists (throws).
        // Return a safe no-op repository when there are no registrations so the app can start
        // even if no OAuth client ids/secrets were provided (dev-friendly).
        if (registrations.isEmpty()) {
            System.out.println("⚠️ No OAuth2 client registrations found — returning empty fallback repository.");
            return new ClientRegistrationRepository() {
                @Override
                public ClientRegistration findByRegistrationId(String registrationId) {
                    return null;
                }
            };
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }


    private ClientRegistration buildClientRegistration(String registrationId,
                                                       OAuth2ClientProperties.Registration registration,
                                                       OAuth2ClientProperties.Provider provider) {

        if (registration == null || registration.getClientId() == null || registration.getClientId().isBlank()) {
            return null;
        }

        try {
            // fallback redirect template
            String redirectUriTemplate = registration.getRedirectUri();
            if (redirectUriTemplate == null || redirectUriTemplate.isBlank()) {
                redirectUriTemplate = "{baseUrl}/login/oauth2/code/{registrationId}";
            }

            // safe scope extraction
            List<String> scopes;
            if (registration.getScope() != null && !registration.getScope().isEmpty()) {
                // Convert Set to List (Spring returns LinkedHashSet)
                scopes = new ArrayList<>(registration.getScope());
            } else {
                scopes = getDefaultScopesFor(registrationId);
            }

            ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(registrationId)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .scope(scopes.toArray(new String[0]))
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri(redirectUriTemplate)
                    .clientName(registration.getClientName() != null
                            ? registration.getClientName()
                            : registrationId);

            // Authorization URI
            if (provider != null && provider.getAuthorizationUri() != null) {
                builder.authorizationUri(provider.getAuthorizationUri());
            } else {
                builder.authorizationUri(getDefaultAuthorizationUri(registrationId));
            }

            // Token URI
            if (provider != null && provider.getTokenUri() != null) {
                builder.tokenUri(provider.getTokenUri());
            } else {
                builder.tokenUri(getDefaultTokenUri(registrationId));
            }

            // User Info URI
            if (provider != null && provider.getUserInfoUri() != null) {
                builder.userInfoUri(provider.getUserInfoUri());
            } else {
                String uri = getDefaultUserInfoUri(registrationId);
                if (uri != null) {
                    builder.userInfoUri(uri);
                }
            }

            // Unique user ID field
            builder.userNameAttributeName(getDefaultUserNameAttributeName(registrationId));

            // Google JWKS
            if ("google".equalsIgnoreCase(registrationId)) {
                builder.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs");
            }

            return builder.build();

        } catch (Exception e) {
            System.err.println("Error building client registration for '" +
                    registrationId + "': " + e.getMessage());
            return null;
        }
    }


    // === Default provider values ===

    private List<String> getDefaultScopesFor(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> List.of("openid", "profile", "email");
            case "github" -> List.of("read:user", "user:email");
            default -> List.of("openid", "profile", "email");
        };
    }

    private String getDefaultAuthorizationUri(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "https://accounts.google.com/o/oauth2/v2/auth";
            case "github" -> "https://github.com/login/oauth/authorize";
            default -> null;
        };
    }

    private String getDefaultTokenUri(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "https://www.googleapis.com/oauth2/v4/token";
            case "github" -> "https://github.com/login/oauth/access_token";
            default -> null;
        };
    }

    private String getDefaultUserInfoUri(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "https://www.googleapis.com/oauth2/v3/userinfo";
            case "github" -> "https://api.github.com/user";
            default -> null;
        };
    }

    private String getDefaultUserNameAttributeName(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "sub";
            case "github"  -> "id";
            default -> "id";
        };
    }


    // === Mask client ID for logs ===
    private String mask(String s) {
        if (s == null) return null;
        if (s.length() <= 6) return "***";
        return s.substring(0, 3) + "***" + s.substring(s.length() - 3);
    }
}
