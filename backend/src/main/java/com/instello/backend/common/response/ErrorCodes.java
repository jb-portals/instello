package com.instello.backend.common.response;

public final class ErrorCodes {

    public static final String NOT_FOUND = "not_found";
    public static final String CONFLICT = "conflict";
    public static final String VALIDATION_FAILED = "validation_failed";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String FORBIDDEN = "forbidden";
    public static final String EMAIL_NOT_VERIFIED = "email_not_verified";
    public static final String RATE_LIMITED = "rate_limited";
    public static final String BAD_REQUEST = "bad_request";
    public static final String INTERNAL_ERROR = "internal_error";

    private ErrorCodes() {}
}
