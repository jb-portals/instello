package com.instello.backend.accessRequest.dto;

import com.instello.backend.common.entity.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record ApproveAccessRequestRequest(
        @Nullable @Size(min = 1, max = 200) String organizationName,
        @Nullable @Email @Size(max = 255) String email,
        @Nullable @Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number") String phone,
        @Nullable @Valid AddressRequest address
) {
}
