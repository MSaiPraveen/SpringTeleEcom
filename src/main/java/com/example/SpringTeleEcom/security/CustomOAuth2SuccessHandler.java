package com.example.SpringTeleEcom.security;

import com.example.SpringTeleEcom.model.Role;
import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.repo.RoleRepository;
import com.example.SpringTeleEcom.repo.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomOAuth2SuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final String frontendRedirectUri;

    public CustomOAuth2SuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            @Value("${app.oauth2.redirect-uri}") String frontendRedirectUri
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Try to extract email & name from Google/GitHub
        String email = (String) attributes.getOrDefault("email", null);
        String name = null;
        String login = null;  // GitHub username

        if (attributes.containsKey("name")) {
            name = (String) attributes.get("name");
        }

        if (attributes.containsKey("login")) {
            // GitHub username
            login = (String) attributes.get("login");
            if (name == null) {
                name = login;  // Use login as name if name is not provided
            }
        }

        if (email == null) {
            // GitHub: sometimes email is null if it's private
            // Fallback to login as "email-like" username
            if (login != null) {
                email = login + "@github.local";
            } else if (name != null) {
                email = name + "@github.local";
            } else {
                // Last resort: use a generic identifier
                email = "user_" + attributes.get("id") + "@oauth.local";
            }
        }

        // Use email as username in our app
        String username = email;

        // Find or create user
        Optional<User> existingUserOpt = userRepository.findByUsername(username);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
        } else {
            // assign ROLE_USER by default
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("ROLE_USER").build()
                    ));

            user = User.builder()
                    .username(username)
                    .password("") // no password, OAuth2 only
                    .fullName(name != null ? name : username)
                    .roles(Set.of(userRole))
                    .build();

            user = userRepository.save(user);
        }

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));

        String token = jwtService.generateToken(user.getUsername());

        // Redirect back to frontend with token and isAdmin flag
        String redirectUrl = String.format(
                "%s?token=%s&username=%s&isAdmin=%s",
                frontendRedirectUri,
                token,
                java.net.URLEncoder.encode(user.getUsername(), "UTF-8"),
                isAdmin
        );

        response.sendRedirect(redirectUrl);
    }
}
