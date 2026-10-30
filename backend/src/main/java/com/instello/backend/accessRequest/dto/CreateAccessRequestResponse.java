package com.instello.backend.accessRequest.dto;

import com.instello.backend.accessRequest.entity.AccessRequestStatus;




public record CreateAccessRequestResponse(
        String id,
        AccessRequestStatus status
) {
}
