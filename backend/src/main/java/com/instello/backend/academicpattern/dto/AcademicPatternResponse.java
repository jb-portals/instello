package com.instello.backend.academicpattern.dto;

import com.instello.backend.academicpattern.entity.AcademicSystemType;
import com.instello.backend.academicpattern.entity.AcademicTemplateKey;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AcademicPatternResponse(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        int durationInYears,
        AcademicSystemType systemType,
        AcademicTemplateKey templateKey,
        boolean canBeEdited,
        List<AcademicStageResponse> stages,
        Instant createdAt,
        Instant updatedAt
) {
}
