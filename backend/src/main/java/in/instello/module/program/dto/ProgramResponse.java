package in.instello.module.program.dto;

import jakarta.annotation.Nullable;
import java.time.Instant;

public record ProgramResponse(
    String id,
    String name,
    String alias,
    Boolean archived,
    Instant createdAt,
    @Nullable Instant updatedAt) {}
