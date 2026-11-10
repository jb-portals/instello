package in.instello.module.academicPattern.dto;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record AcademicStageResponse(
    String id,
    String name,
    String alias,
    @Nullable String description,
    int sequenceNumber,
    int yearNumber,
    Instant createdAt,
    Instant updatedAt) {}
