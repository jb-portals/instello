package in.instello.module.program.dto;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ProgramResponse(
    String id,
    String name,
    String alias,
    Boolean archived,
    Instant createdAt,
    @Nullable Instant updatedAt) {}
