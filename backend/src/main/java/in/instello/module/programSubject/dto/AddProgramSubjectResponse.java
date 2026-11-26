package in.instello.module.programSubject.dto;

import in.instello.module.programSubject.entity.ProgramSubject;
import org.jspecify.annotations.Nullable;

public record AddProgramSubjectResponse(String id, @Nullable String description) {
  public static AddProgramSubjectResponse fromProgram(ProgramSubject programSubject) {
    return new AddProgramSubjectResponse(programSubject.getId(), programSubject.getDescription());
  }
}
