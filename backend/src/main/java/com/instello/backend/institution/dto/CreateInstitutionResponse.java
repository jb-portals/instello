package com.instello.backend.institution.dto;

import com.instello.backend.institution.entity.Institution;

public record CreateInstitutionResponse(
        String id,
        String name,
        String slug
){
    public static CreateInstitutionResponse fromInstitution(Institution institution){
        return new CreateInstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getSlug()
        );
    }
}
