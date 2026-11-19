package in.instello.module.program.dto;

import in.instello.module.program.Program;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

public record UpdateProgramRequest(
    @Nullable
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String name) {
  public void applyTo(Program program) {
    if (name != null) {
      program.setName(name.strip());
    }
  }
}
