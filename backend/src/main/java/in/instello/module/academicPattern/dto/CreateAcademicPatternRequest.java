package in.instello.module.academicPattern.dto;

import in.instello.module.academicPattern.entity.AcademicSystemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record CreateAcademicPatternRequest(
    @NotBlank @Size(max = 200) String name,
    @Nullable @Size(max = 2000) String description,
    @Min(1) @Max(20) int durationInYears,
    @NotNull AcademicSystemType systemType) {}
