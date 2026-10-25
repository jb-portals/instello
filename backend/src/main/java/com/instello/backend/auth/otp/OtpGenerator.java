package com.instello.backend.auth.otp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {

    private static final int OTP_LENGTH = 6;
    private static final int BOUND = (int) Math.pow(10, OTP_LENGTH);
    private static final Pattern OTP_PATTERN = Pattern.compile("\\d{" + OTP_LENGTH + "}");

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt(BOUND));
    }

    public boolean verify(String input, String expected) {
        if (input == null || expected == null || !OTP_PATTERN.matcher(input).matches()) {
            return false;
        }
        return MessageDigest.isEqual(
                input.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}