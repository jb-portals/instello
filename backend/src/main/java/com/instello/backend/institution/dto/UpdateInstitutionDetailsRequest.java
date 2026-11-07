package com.instello.backend.institution.dto;

import com.instello.backend.common.entity.dto.AddressRequest;
import org.jspecify.annotations.Nullable;
import jakarta.validation.constraints.NotBlank;

public record UpdateInstitutionDetailsRequest(
   @Nullable @NotBlank String name,
   @Nullable @NotBlank AddressRequest address
) {}
