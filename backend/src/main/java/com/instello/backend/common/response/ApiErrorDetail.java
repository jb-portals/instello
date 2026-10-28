package com.instello.backend.common.response;

public record ApiErrorDetail(
        String code,
        String target,
        String message
) {
}
