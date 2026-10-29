package com.instello.backend.academicpattern.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateAcademicStageRequest(
        @Size(max = 200) String name,
        @Size(max = 100) String alias,
        @Size(max = 2000) String description,
        @Min(1) Integer sequenceNumber,
        @Min(1) @Max(20) Integer yearNumber
) {
}
