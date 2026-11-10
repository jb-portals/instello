package in.instello.module.academicPattern.dto;

import in.instello.module.academicPattern.entity.AcademicSystemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateAcademicPatternRequest(
    @Nullable @Size(max = 200) String name,
    @Nullable @Size(max = 2000) String description,
    @Nullable @Min(1) @Max(20) Integer durationInYears,
    @Nullable AcademicSystemType systemType) {}
