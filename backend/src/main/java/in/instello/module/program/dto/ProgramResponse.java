package in.instello.module.program.dto;

import in.instello.module.program.Program;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ProgramResponse(
    String id,
    String name,
    String alias,
    Boolean archived,
    Instant createdAt,
    @Nullable Instant updatedAt) {

  public static ProgramResponse from(Program program) {
    return new ProgramResponse(
        program.getId(),
        program.getName(),
        program.getAlias(),
        program.isArchive(),
        program.getCreatedAt(),
        program.getUpdatedAt());
  }
}
