package com.instello.backend.auth.otp;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("instello.auth.otp")
public record OtpProperties(
        Duration ttl,
        Duration resendCooldown,
        int maxRequestsPerHour,
        int maxVerifyAttempts
) {
}
