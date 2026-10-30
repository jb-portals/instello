package com.instello.backend.academicPattern.dto;

import com.instello.backend.academicPattern.entity.AcademicSystemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateAcademicPatternRequest(
        @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @Min(1) @Max(20) Integer durationInYears,
        AcademicSystemType systemType
) {
}
