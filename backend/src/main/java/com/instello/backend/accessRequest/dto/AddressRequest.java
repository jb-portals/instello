package com.instello.backend.accessRequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 255) String addressLine,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @Size(max = 100) String country
) {
}
