package com.inventory.config;

import com.inventory.modules.users.entity.Role;
import com.inventory.modules.users.entity.User;
import com.inventory.modules.users.repository.RoleRepository;
import com.inventory.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String... args) {
        boolean hasAdminConfiguration = !adminUsername.isBlank()
                || !adminEmail.isBlank()
                || !adminPassword.isBlank()
                || !adminFullName.isBlank();
        if (!hasAdminConfiguration) {
            log.info("No bootstrap admin configured. Skipping admin initialization.");
            return;
        }

        requireConfigured(adminUsername, "ADMIN_USERNAME");
        requireConfigured(adminEmail, "ADMIN_EMAIL");
        requireConfigured(adminPassword, "ADMIN_PASSWORD");
        requireConfigured(adminFullName, "ADMIN_FULL_NAME");

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists. Skipping initialization.", adminUsername);
            return;
        }

        if (adminPassword.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found. Create it in the database before starting the application."));

        User admin = User.builder()
                .fullName(adminFullName)
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .active(true)
                .role(adminRole)
                .build();

        userRepository.save(admin);
        log.info("Initial admin user '{}' created successfully.", adminUsername);
    }

    private void requireConfigured(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured before starting the application.");
        }
    }
}
