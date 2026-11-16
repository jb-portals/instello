package in.instello.module.subjectCatalog.dto;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record SubjectResponse(
    String id,
    String name,
    String colorHash,
    String code,
    String alias,
    @Nullable String description,
    Boolean archived,
    Instant createdAt,
    @Nullable Instant updatedAt) {}
