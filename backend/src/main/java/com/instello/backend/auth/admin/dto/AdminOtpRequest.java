package com.instello.backend.auth.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminOtpRequest(
        @NotBlank @Email String email
) {
}
