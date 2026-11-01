package com.instello.backend.common.exception.response;

public record ApiErrorDetail(
        String code,
        String target,
        String message
) {
}
