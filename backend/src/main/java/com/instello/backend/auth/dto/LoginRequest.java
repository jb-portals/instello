package com.instello.backend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
