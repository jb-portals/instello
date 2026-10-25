package com.instello.backend.auth.otp;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.instello.backend.common.notification.email.EmailTemplateService;
import com.instello.backend.common.redis.RedisService;
import com.instello.backend.user.entity.PlatformRole;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

@Service
public class OtpService {

    private static final Duration REQUEST_WINDOW = Duration.ofHours(1);

    private final OtpGenerator otpGenerator;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final EmailTemplateService emailTemplateService;
    private final OtpProperties properties;

    public OtpService(
            OtpGenerator otpGenerator,
            RedisService redisService,
            UserRepository userRepository,
            EmailTemplateService emailTemplateService,
            OtpProperties properties
    ) {
        this.otpGenerator = otpGenerator;
        this.redisService = redisService;
        this.userRepository = userRepository;
        this.emailTemplateService = emailTemplateService;
        this.properties = properties;
    }

    public void requestAdminOtp(String email) {
        String normalizedEmail = normalize(email);
        String prefix = keyPrefix(OtpPurpose.ADMIN_LOGIN, normalizedEmail);

        String cooldownKey = prefix + ":cooldown";
        if (!redisService.setIfAbsent(cooldownKey, "1", properties.resendCooldown())) {
            throw new OtpRateLimitException(retryAfter(cooldownKey));
        }

        String requestsKey = prefix + ":requests";
        if (redisService.increment(requestsKey, REQUEST_WINDOW) > properties.maxRequestsPerHour()) {
            throw new OtpRateLimitException(retryAfter(requestsKey));
        }

        Optional<User> admin = findAdmin(normalizedEmail);
        if (admin.isEmpty()) {
            return;
        }

        String code = otpGenerator.generate();
        redisService.set(prefix + ":code", code, properties.ttl());
        redisService.delete(prefix + ":attempts");
        emailTemplateService.sendOtp(admin.get().getEmail(), code, (int) properties.ttl().toMinutes());
    }

    public User verifyAdminOtp(String email, String code) {
        String normalizedEmail = normalize(email);
        String prefix = keyPrefix(OtpPurpose.ADMIN_LOGIN, normalizedEmail);
        String codeKey = prefix + ":code";
        String attemptsKey = prefix + ":attempts";

        String expected = redisService.get(codeKey).orElseThrow(InvalidOtpException::new);

        if (!otpGenerator.verify(code, expected)) {
            long attempts = redisService.increment(attemptsKey, properties.ttl());
            if (attempts >= properties.maxVerifyAttempts()) {
                redisService.delete(codeKey);
                redisService.delete(attemptsKey);
            }
            throw new InvalidOtpException();
        }

        redisService.delete(codeKey);
        redisService.delete(attemptsKey);

        return findAdmin(normalizedEmail).orElseThrow(InvalidOtpException::new);
    }

    private Optional<User> findAdmin(String normalizedEmail) {
        return userRepository.findByEmailIgnoreCaseAndRoles_Role(normalizedEmail, PlatformRole.ADMIN);
    }

    private long retryAfter(String key) {
        return Math.max(1, redisService.getRemainingExpiration(key));
    }

    private static String keyPrefix(OtpPurpose purpose, String normalizedEmail) {
        return "otp:" + purpose.key() + ":" + normalizedEmail;
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
