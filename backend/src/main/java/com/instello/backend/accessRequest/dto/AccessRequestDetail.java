package com.instello.backend.accessRequest.dto;

import com.instello.backend.accessRequest.entity.AccessRequestStatus;
import com.instello.backend.invitation.entity.InvitationStatus;


import java.time.Instant;

public record AccessRequestDetail(
        String id,
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
