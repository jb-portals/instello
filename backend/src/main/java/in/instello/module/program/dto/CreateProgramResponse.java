package in.instello.module.program.dto;

import in.instello.module.program.Program;

public record CreateProgramResponse(String id, String name) {
  public static CreateProgramResponse fromProgram(Program program) {
    return new CreateProgramResponse(program.getId(), program.getName());
  }
}
