package in.instello.module.subjectCatalog.dto;

import in.instello.module.subjectCatalog.Subject;

public record SubjectArchiveResponse(String id, String name, Boolean archive) {
  public static SubjectArchiveResponse from(Subject subject) {
    return new SubjectArchiveResponse(subject.getId(), subject.getName(), subject.isArchive());
  }
}
