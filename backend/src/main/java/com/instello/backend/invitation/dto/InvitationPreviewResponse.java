package com.instello.backend.invitation.dto;

import java.time.Instant;

public record InvitationPreviewResponse(
        String email,
        String organizationName,
        Instant expiresAt
) {
}
