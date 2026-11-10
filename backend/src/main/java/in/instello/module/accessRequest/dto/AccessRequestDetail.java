package in.instello.module.accessRequest.dto;

import in.instello.module.accessRequest.entity.AccessRequestStatus;
import in.instello.common.address.dto.AddressResponse;
import in.instello.module.invitation.entity.InvitationStatus;
import org.jspecify.annotations.Nullable;


import java.time.Instant;

public record AccessRequestDetail(
        String id,
        String organizationName,
        String firstName,
        String lastName,
        String email,
        String phone,
        @Nullable AddressResponse address,
        @Nullable String message,
        AccessRequestStatus status,
        @Nullable String adminNotes,
        @Nullable String rejectionReason,
        @Nullable String reviewedBy,
        @Nullable Instant reviewedAt,
        @Nullable OrganizationInfo organization,
        @Nullable InvitationInfo invitation,
        Instant createdAt,
        Instant updatedAt
) {

    public record OrganizationInfo(String id, String name) {
    }

    public record InvitationInfo(
            String id,
            String email,
            InvitationStatus status,
            Instant expiresAt,
            Instant createdAt
    ) {
    }
}
