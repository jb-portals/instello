package com.instello.backend.academicPattern.dto;
import org.jspecify.annotations.Nullable;



import java.time.Instant;

public record AcademicStageResponse(
        String id,
        String name,
        String alias,
        @Nullable String description,
        int sequenceNumber,
        int yearNumber,
        Instant createdAt,
        Instant updatedAt
) {
}
