package com.instello.backend.accessRequest.dto;

import com.instello.backend.accessRequest.entity.AccessRequestStatus;
import jakarta.validation.constraints.Size;

public record UpdateAccessRequestRequest(
        AccessRequestStatus status,
        @Size(max = 5000) String adminNotes
) {
}
