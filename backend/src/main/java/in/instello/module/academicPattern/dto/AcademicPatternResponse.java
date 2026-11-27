package in.instello.module.academicPattern.dto;

import in.instello.module.academicPattern.entity.AcademicSystemType;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record AcademicPatternResponse(
    String id,
    String organizationId,
    String name,
    @Nullable String description,
    int durationInYears,
    AcademicSystemType systemType,
    boolean locked,
    boolean canBeEdited,
    List<AcademicStageResponse> stages,
    Instant createdAt,
    Instant updatedAt) {}
