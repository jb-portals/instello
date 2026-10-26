package com.instello.backend.user.bootstrap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.instello.backend.user.entity.PlatformRole;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.entity.UserPlatformRole;
import com.instello.backend.user.repository.UserRepository;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final BootstrapAdminProperties properties;
    private final TransactionTemplate transactionTemplate;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            BootstrapAdminProperties properties,
            TransactionTemplate transactionTemplate
    ) {
        this.userRepository = userRepository;
        this.properties = properties;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        String configuredEmail = properties.email();
        if (configuredEmail == null || configuredEmail.isBlank()) {
            logger.info("No bootstrap admin email configured, skipping admin bootstrap");
            return;
        }
        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);

        try {
            transactionTemplate.executeWithoutResult(status -> ensureAdmin(email));
        } catch (DataIntegrityViolationException exception) {
            logger.warn("Bootstrap admin was created concurrently by another instance");
        }
    }

    private void ensureAdmin(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        boolean isNewUser = user == null;
        if (isNewUser) {
            user = User.builder()
                    .email(email)
                    .emailVerifiedAt(Instant.now())
                    .build();
        }
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getRole() == PlatformRole.ADMIN);
        if (isAdmin) {
            logger.info("Bootstrap admin already exists");
            return;
        }

        user.getRoles().add(UserPlatformRole.builder()
                .role(PlatformRole.ADMIN)
                .user(user)
                .build());
        userRepository.save(user);

        if (isNewUser) {
            logger.info("✅ Created bootstrap admin");
        } else {
            logger.info("Granted ADMIN role to existing user");
        }
    }
}
