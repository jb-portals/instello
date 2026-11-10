package in.instello.module.user.bootstrap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import in.instello.module.user.entity.AppAuthority;
import in.instello.module.user.entity.Authority;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;

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
        var existingUser = userRepository.findByEmailIgnoreCase(email);
        boolean isNewUser = existingUser.isEmpty();
        User user = existingUser.orElseGet(() -> User.builder()
                    .email(email)
                    .emailVerifiedAt(Instant.now())
                    .build());
        List<Authority> authorities = user.getAuthorities();
        if (authorities == null) {
            authorities = new ArrayList<>();
            user.setAuthorities(authorities);
        }

        boolean isAdmin = authorities.stream()
                .anyMatch(authority -> authority.getAuthority() == AppAuthority.ROLE_ADMIN);
        if (isAdmin) {
            logger.info("Bootstrap admin already exists");
            return;
        }

        authorities.add(Authority.builder()
                .authority(AppAuthority.ROLE_ADMIN)
                .user(user)
                .build());
        userRepository.save(user);

        if (isNewUser) {
            logger.info("✅ Created bootstrap admin");
        } else {
            logger.info("Granted ROLE_ADMIN authority to existing user");
        }
    }
}
