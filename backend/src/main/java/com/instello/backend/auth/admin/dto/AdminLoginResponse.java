package com.instello.backend.auth.admin.dto;

import java.util.UUID;

public record AdminLoginResponse(
        UUID id,
        String email
) {
}
