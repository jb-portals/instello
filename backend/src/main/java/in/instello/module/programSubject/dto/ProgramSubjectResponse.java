package in.instello.module.programSubject.dto;

import in.instello.module.programSubject.entity.ProgramSubject;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ProgramSubjectResponse(
    String id,
    @Nullable String description,
    Boolean archive,
    Instant createdAt,
    @Nullable Instant updatedAt) {

  public static ProgramSubjectResponse from(ProgramSubject programSubject) {
    return new ProgramSubjectResponse(
        programSubject.getId(),
        programSubject.getDescription(),
        programSubject.isArchive(),
        programSubject.getCreatedAt(),
        programSubject.getUpdatedAt());
  }
}
