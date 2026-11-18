package in.instello.module.program.dto;

import in.instello.module.program.Program;

public record ProgramArchiveResponse(String id, String name, Boolean archive) {
  public static ProgramArchiveResponse from(Program program) {
    return new ProgramArchiveResponse(program.getId(), program.getName(), program.isArchive());
  }
}
