package com.instello.backend.accessRequest.dto;

import com.instello.backend.accessRequest.entity.AccessRequest;
import com.instello.backend.accessRequest.entity.AccessRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record AccessRequestSummary(
        UUID id,
        String organizationName,
        String firstName,
        String lastName,
        String email,
        String phone,
        String city,
        String state,
        AccessRequestStatus status,
        Instant createdAt
) {

    public static AccessRequestSummary from(AccessRequest request) {
        return new AccessRequestSummary(
                request.getId(),
                request.getOrganizationName(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress() != null ? request.getAddress().getCity() : null,
                request.getAddress() != null ? request.getAddress().getState() : null,
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
