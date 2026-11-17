package in.instello.module.subjectCatalog.dto;

import in.instello.module.subjectCatalog.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

public record UpdateSubjectRequest(
    @Nullable
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String name,
    @Nullable
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String code,
    @Nullable
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String colorHash,
    @Nullable @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String description) {
  public void applyTo(Subject subject) {
    if (name != null) {
      subject.setName(name.strip());
    }
    if (code != null) {
      subject.setCode(code.strip());
    }
    if (colorHash != null) {
      subject.setColorHash(colorHash.strip());
    }
    if (description != null) {
      subject.setDescription(description.isBlank() ? null : description.strip());
    }
  }
}
