package in.instello.module.subjectCatalog.dto;

import in.instello.module.subjectCatalog.Subject;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record SubjectResponse(
    String id,
    String name,
    String colorHash,
    String code,
    String alias,
    @Nullable String description,
    Boolean archive,
    Instant createdAt,
    @Nullable Instant updatedAt) {

  public static SubjectResponse from(Subject subject) {
    return new SubjectResponse(
        subject.getId(),
        subject.getName(),
        subject.getColorHash(),
        subject.getCode(),
        subject.getAlias(),
        subject.getDescription() != null ? subject.getDescription() : null,
        subject.isArchive(),
        subject.getCreatedAt(),
        subject.getUpdatedAt());
  }
}
