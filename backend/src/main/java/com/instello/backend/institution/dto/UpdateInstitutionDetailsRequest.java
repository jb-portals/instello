package com.instello.backend.institution.dto;

import com.instello.backend.common.entity.dto.AddressRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record UpdateInstitutionDetailsRequest(
   @Nullable @NotBlank String name,
   @Nullable @NotBlank AddressRequest address
) {}
