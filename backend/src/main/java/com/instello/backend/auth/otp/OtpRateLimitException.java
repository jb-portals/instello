package com.instello.backend.auth.otp;

import lombok.Getter;

@Getter
public class OtpRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public OtpRateLimitException(long retryAfterSeconds) {
        super("Too many OTP requests. Try again later.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

}
