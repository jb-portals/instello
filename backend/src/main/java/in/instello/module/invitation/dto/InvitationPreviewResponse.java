package in.instello.module.invitation.dto;

import java.time.Instant;

public record InvitationPreviewResponse(
        String email,
        String organizationName,
        Instant expiresAt
) {
}
