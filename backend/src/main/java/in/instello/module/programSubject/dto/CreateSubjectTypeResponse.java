package in.instello.module.programSubject.dto;

import in.instello.module.programSubject.entity.SubjectType;

public record CreateSubjectTypeResponse(String id, String name) {
  public static CreateSubjectTypeResponse fromSubjectType(SubjectType subjectType) {
    return new CreateSubjectTypeResponse(subjectType.getId(), subjectType.getName());
  }
}
