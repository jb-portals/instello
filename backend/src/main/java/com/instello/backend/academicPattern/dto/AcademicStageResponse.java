package com.instello.backend.academicPattern.dto;



import java.time.Instant;

public record AcademicStageResponse(
        String id,
        String name,
        String alias,
        String description,
        int sequenceNumber,
        int yearNumber,
        Instant createdAt,
        Instant updatedAt
) {
}
