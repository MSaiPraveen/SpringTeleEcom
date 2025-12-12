package com.example.SpringTeleEcom.security;

import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        System.out.println("🔍 CustomUserDetailsService - Loading user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("❌ User NOT FOUND in database: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        System.out.println("✅ User found: " + username);
        System.out.println("   Roles: " + user.getRoles().size());

        if (user.getRoles().isEmpty()) {
            System.err.println("⚠️  WARNING: User has NO ROLES: " + username);
        }

        user.getRoles().forEach(role ->
            System.out.println("   - " + role.getName())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword() != null ? user.getPassword() : "",  // Handle null password for OAuth users
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}
