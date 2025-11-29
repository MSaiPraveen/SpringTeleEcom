package com.example.SpringTeleEcom;

import com.example.SpringTeleEcom.model.Role;
import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.repo.RoleRepository;
import com.example.SpringTeleEcom.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class SpringTeleEcomApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTeleEcomApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepo,
                                RoleRepository roleRepo,
                                PasswordEncoder encoder) {
        return args -> {
            if (userRepo.findByUsername("admin").isEmpty()) {

                Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                        .orElseGet(() -> roleRepo.save(
                                Role.builder().name("ROLE_ADMIN").build()
                        ));

                User admin = User.builder()
                        .username("admin")
                        .password(encoder.encode("admin123"))
                        .fullName("Admin User")
                        .roles(Set.of(adminRole))
                        .build();

                userRepo.save(admin);
                System.out.println("✅ Admin user created: admin/admin123");
            }
        };
    }
}
