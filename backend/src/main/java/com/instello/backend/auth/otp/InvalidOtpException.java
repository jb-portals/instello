package com.instello.backend.auth.otp;

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException() {
        super("Invalid or expired OTP");
    }
}
