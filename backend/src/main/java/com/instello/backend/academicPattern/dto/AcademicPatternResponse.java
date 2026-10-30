package com.instello.backend.academicPattern.dto;

import com.instello.backend.academicPattern.entity.AcademicSystemType;
import com.instello.backend.academicPattern.entity.AcademicTemplateKey;


import java.time.Instant;
import java.util.List;

public record AcademicPatternResponse(
        String id,
        String organizationId,
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
