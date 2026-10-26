package com.instello.backend.auth.otp;

public enum OtpPurpose {
    ADMIN_LOGIN("admin_login"),
    PASSWORD_RESET("password_reset"),
    EMAIL_VERIFICATION("email_verification");

    private final String key;

    OtpPurpose(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
