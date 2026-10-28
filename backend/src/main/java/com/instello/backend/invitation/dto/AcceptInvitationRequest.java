package com.instello.backend.invitation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(
        @NotBlank String token,
        // 72 is the BCrypt input limit in bytes
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
