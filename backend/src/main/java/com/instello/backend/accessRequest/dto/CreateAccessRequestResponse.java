package com.instello.backend.accessRequest.dto;

import com.instello.backend.accessRequest.entity.AccessRequestStatus;

import java.util.UUID;

public record CreateAccessRequestResponse(
        UUID id,
        AccessRequestStatus status
) {
}
