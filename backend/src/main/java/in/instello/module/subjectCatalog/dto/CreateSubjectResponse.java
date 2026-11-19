package in.instello.module.subjectCatalog.dto;

import in.instello.module.subjectCatalog.Subject;

public record CreateSubjectResponse(String id, String name) {
  public static CreateSubjectResponse fromSubject(Subject subject) {
    return new CreateSubjectResponse(subject.getId(), subject.getName());
  }
}
