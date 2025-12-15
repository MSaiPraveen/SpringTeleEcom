package com.example.SpringTeleEcom;

import com.example.SpringTeleEcom.model.Role;
import com.example.SpringTeleEcom.model.User;
import com.example.SpringTeleEcom.repo.RoleRepository;
import com.example.SpringTeleEcom.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Set;

@Slf4j
@SpringBootApplication
public class SpringTeleEcomApplication {

    public static void main(String[] args) {
        log.info("🚀 Starting SpringTeleEcom Application...");
        SpringApplication.run(SpringTeleEcomApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String profile = String.join(", ", env.getActiveProfiles());
        if (profile.isEmpty()) profile = "default";

        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║  🎉 SpringTeleEcom v6.0.1 Started Successfully!                ║");
            log.info("╠════════════════════════════════════════════════════════════════╣");
            log.info("║  📍 Local:      http://localhost:{}                          ║", port);
            log.info("║  🌐 Network:    http://{}:{}                         ║", host, port);
            log.info("║  🏥 Health:     http://localhost:{}/health                    ║", port);
            log.info("║  📊 Actuator:   http://localhost:{}/actuator                  ║", port);
            log.info("║  🔧 Profile:    {}                                            ║", profile);
            log.info("║  ⏰ Started at: {}                         ║", Instant.now());
            log.info("╚════════════════════════════════════════════════════════════════╝");
        } catch (Exception e) {
            log.warn("Could not determine network address", e);
        }
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailed(ApplicationFailedEvent event) {
        log.error("💥 APPLICATION FAILED TO START!");
        log.error("💥 Reason: {}", event.getException().getMessage(), event.getException());
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        log.info("🛑 SpringTeleEcom Application shutting down gracefully...");
        log.info("👋 Goodbye!");
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
