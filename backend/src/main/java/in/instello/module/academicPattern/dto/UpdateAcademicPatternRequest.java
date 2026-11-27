package in.instello.module.academicPattern.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateAcademicPatternRequest(
    @Nullable @Size(max = 200) String name, @Nullable @Size(max = 2000) String description) {

  @JsonAnySetter
  public void rejectUnsupportedField(String field, @Nullable Object value) {
    throw new IllegalArgumentException("Academic pattern field cannot be updated: " + field);
  }
}
