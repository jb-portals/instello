package com.instello.backend.accessRequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectAccessRequestRequest(
        @NotBlank @Size(max = 2000) String reason
) {
}
