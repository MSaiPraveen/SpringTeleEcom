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

        System.out.println("🔐 OAuth2 Login Success:");
        System.out.println("   Provider: " + (login != null ? "GitHub" : "Google"));
        System.out.println("   Username: " + username);
        System.out.println("   Name: " + name);

        // Find or create user
        Optional<User> existingUserOpt = userRepository.findByUsername(username);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            System.out.println("✅ Existing user found: " + username);
            System.out.println("   User ID: " + user.getId());
            System.out.println("   Roles: " + user.getRoles().size());
        } else {
            System.out.println("📝 Creating new OAuth user: " + username);

            // assign ROLE_USER by default
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        System.out.println("   Creating ROLE_USER...");
                        return roleRepository.save(
                                Role.builder().name("ROLE_USER").build()
                        );
                    });

            System.out.println("   Role assigned: " + userRole.getName() + " (ID: " + userRole.getId() + ")");

            user = User.builder()
                    .username(username)
                    .password("") // no password, OAuth2 only
                    .fullName(name != null ? name : username)
                    .roles(Set.of(userRole))
                    .build();

            user = userRepository.save(user);
            System.out.println("✅ User created and saved:");
            System.out.println("   User ID: " + user.getId());
            System.out.println("   Username: " + user.getUsername());
            System.out.println("   Roles: " + user.getRoles().size());
        }

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));

        System.out.println("   Is Admin: " + isAdmin);

        String token = jwtService.generateToken(user.getUsername());

        System.out.println("🎫 JWT Token generated for: " + user.getUsername());
        System.out.println("   Token length: " + token.length());

        // Redirect back to frontend with token and isAdmin flag
        String redirectUrl = String.format(
                "%s?token=%s&username=%s&isAdmin=%s",
                frontendRedirectUri,
                token,
                java.net.URLEncoder.encode(user.getUsername(), "UTF-8"),
                isAdmin
        );

        System.out.println("↗️  Redirecting to: " + frontendRedirectUri);
        response.sendRedirect(redirectUrl);
    }
}
