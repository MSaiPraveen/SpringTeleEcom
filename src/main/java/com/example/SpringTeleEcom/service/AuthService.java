package com.example.SpringTeleEcom.service;

import com.example.SpringTeleEcom.model.Role;
import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.repo.RoleRepository;
import com.example.SpringTeleEcom.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String password, String fullName) {
        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").build()
                ));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(user);
    }
}
