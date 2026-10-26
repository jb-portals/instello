package com.instello.backend.accessRequest.dto;

import com.instello.backend.accessRequest.entity.AccessRequestStatus;
import com.instello.backend.invitation.entity.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record AccessRequestDetail(
        UUID id,
        String organizationName,
        String firstName,
        String lastName,
        String email,
        String phone,
        AddressResponse address,
        String message,
        AccessRequestStatus status,
        String adminNotes,
        String rejectionReason,
        String reviewedBy,
        Instant reviewedAt,
        OrganizationInfo organization,
        InvitationInfo invitation,
        Instant createdAt,
        Instant updatedAt
) {

    public record OrganizationInfo(UUID id, String name) {
    }

    public record InvitationInfo(
            UUID id,
            String email,
            InvitationStatus status,
            Instant expiresAt,
            Instant createdAt
    ) {
    }
}
