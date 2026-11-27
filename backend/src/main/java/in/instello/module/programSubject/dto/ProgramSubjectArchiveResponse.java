package in.instello.module.programSubject.dto;

import in.instello.module.programSubject.entity.ProgramSubject;

public record ProgramSubjectArchiveResponse(String id, Boolean archive) {
  public static ProgramSubjectArchiveResponse from(ProgramSubject programSubject) {
    return new ProgramSubjectArchiveResponse(programSubject.getId(), programSubject.isArchive());
  }
}
