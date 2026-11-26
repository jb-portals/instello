package in.instello.module.programSubject.dto;

import in.instello.module.programSubject.entity.SubjectType;

public record SubjectTypeArchiveResponse(String id, String name, Boolean archive) {
  public static SubjectTypeArchiveResponse from(SubjectType subjectType) {
    return new SubjectTypeArchiveResponse(
        subjectType.getId(), subjectType.getName(), subjectType.isArchive());
  }
}
