package com.example.SpringTeleEcom.controller;

import com.example.SpringTeleEcom.model.dto.*;
import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.security.JwtService;
import com.example.SpringTeleEcom.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),   // ⬅️ record accessor
                        request.password()    // ⬅️ record accessor
                )
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(token, user.getUsername(), isAdmin)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        User newUser = authService.registerUser(
                request.username(),   // ⬅️ record accessor
                request.password(),   // ⬅️ record accessor
                request.fullName()    // ⬅️ record accessor
        );

        String token = jwtService.generateToken(newUser.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(token, newUser.getUsername(), false)
        );
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(@RequestBody RegisterRequest request) {

        System.out.println("🔑 Admin registration request for: " + request.username());

        User newAdmin = authService.registerAdmin(
                request.username(),
                request.password(),
                request.fullName()
        );

        String token = jwtService.generateToken(newAdmin.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(token, newAdmin.getUsername(), true)
        );
    }
}
