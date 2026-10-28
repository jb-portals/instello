package com.instello.backend.auth.dto;

public record CsrfTokenResponse(
        String token,
        String headerName,
        String parameterName
) {
}
