package in.instello.module.programSubject.dto;

import in.instello.module.programSubject.entity.SubjectType;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record SubjectTypeResponse(
    String id,
    String name,
    Boolean builtIn,
    Boolean archive,
    Instant createdAt,
    @Nullable Instant updatedAt) {

  public static SubjectTypeResponse from(SubjectType subjectType) {
    return new SubjectTypeResponse(
        subjectType.getId(),
        subjectType.getName(),
        subjectType.isBuiltIn(),
        subjectType.isArchive(),
        subjectType.getCreatedAt(),
        subjectType.getUpdatedAt());
  }
}
