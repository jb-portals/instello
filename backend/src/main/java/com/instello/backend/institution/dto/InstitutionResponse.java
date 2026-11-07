package com.instello.backend.institution.dto;

import com.instello.backend.common.entity.dto.AddressResponse;
import com.instello.backend.institution.entity.Institution;
import jakarta.annotation.Nullable;

import java.time.Instant;

public record InstitutionResponse(
        String id,
        String name,
        @Nullable String logo,
        String code,
        Instant createdAt,
        AddressResponse address
) {

    public static InstitutionResponse from(Institution institution){
        AddressResponse addressResponse = AddressResponse.from(institution.getAddress());

        return  new InstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getLogo(),
                institution.getCode(),
                institution.getCreatedAt(),
                addressResponse
        );
    }
}
