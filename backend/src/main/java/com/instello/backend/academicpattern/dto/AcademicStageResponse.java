package com.instello.backend.academicpattern.dto;

import java.time.Instant;
import java.util.UUID;

public record AcademicStageResponse(
        UUID id,
        String name,
        String alias,
        String description,
        int sequenceNumber,
        int yearNumber,
        Instant createdAt,
        Instant updatedAt
) {
}
