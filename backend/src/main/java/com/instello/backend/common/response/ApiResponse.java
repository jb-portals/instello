package com.instello.backend.common.response;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiResponse<T>(
        int code,
        String status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> of(
            HttpStatus httpStatus,
            String message,
            T data
    ) {
        return new ApiResponse<>(
                httpStatus.value(),
                httpStatus.name(),
                message,
                data
        );
    }

    public static <T> ApiResponse<T> ok(
            String message,
            T data
    ) {
        return of(HttpStatus.OK, message, data);
    }

    public static ApiResponse<Void> ok(String message) {
        return of(HttpStatus.OK, message, null);
    }

    public static <T> ApiResponse<T> created(
            String message,
            T data
    ) {
        return of(HttpStatus.CREATED, message, data);
    }
}
