package com.instello.backend.accessRequest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ApproveAccessRequestRequest(
        @Size(min = 1, max = 200) String organizationName,
        @Email @Size(max = 255) String email,
        @Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number") String phone,
        @Valid AddressRequest address
) {
}
