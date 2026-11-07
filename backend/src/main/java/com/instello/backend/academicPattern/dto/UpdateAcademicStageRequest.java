package com.instello.backend.academicPattern.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateAcademicStageRequest(
        @Nullable @Size(max = 200) String name,
        @Nullable @Size(max = 100) String alias,
        @Nullable @Size(max = 2000) String description,
        @Nullable @Min(1) Integer sequenceNumber,
        @Nullable @Min(1) @Max(20) Integer yearNumber
) {
}
