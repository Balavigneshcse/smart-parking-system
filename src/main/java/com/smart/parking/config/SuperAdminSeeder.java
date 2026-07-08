package com.smart.parking.config;

import com.smart.parking.domain.User;
import com.smart.parking.domain.enums.UserRole;
import com.smart.parking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@Order(1)
public class SuperAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AppProperties props;

    public SuperAdminSeeder(UserRepository userRepo, PasswordEncoder encoder, AppProperties props) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            AppProperties.SuperAdmin sa = props.getSuperAdmin();
            if (!userRepo.existsByEmail(sa.getEmail())) {
                userRepo.save(User.builder()
                        .name(sa.getName())
                        .email(sa.getEmail())
                        .password(encoder.encode(sa.getPassword()))
                        .phone(sa.getPhone())
                        .role(UserRole.SUPER_ADMIN)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build());
                log.info("✅ Super Admin seeded: {}", sa.getEmail());
            } else {
                log.info("ℹ️  Super Admin already exists — skipping.");
            }
        } catch (Exception e) {
            log.error("❌ SuperAdminSeeder failed: {}", e.getMessage());
        }
    }
}
