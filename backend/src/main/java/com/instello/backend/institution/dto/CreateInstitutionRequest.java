package com.instello.backend.institution.dto;

import com.instello.backend.common.entity.dto.AddressRequest;
import com.instello.backend.institution.entity.Institution;
import jakarta.validation.constraints.NotNull;

public record CreateInstitutionRequest(
        @NotNull String name,
        @NotNull String code,
        @NotNull String slug,
        @NotNull AddressRequest address
) {

        public Institution toInstitution() {
                return Institution.builder()
                        .name(name().trim())
                        .code(code().trim())
                        .slug(slug().trim())
                        .address(address().toAddress())
                        .build();
        }
}