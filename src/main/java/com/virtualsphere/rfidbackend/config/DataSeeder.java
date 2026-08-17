package com.virtualsphere.rfidbackend.config;

import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the same default admin login as the desktop app (admin / admin123) if
 * this backend is ever pointed at a fresh database first. If the desktop app
 * already created it, this is a no-op.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsernameIgnoreCase("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Seeded default admin user (username: admin / password: admin123). Please change it after first login.");
        }
    }
}
