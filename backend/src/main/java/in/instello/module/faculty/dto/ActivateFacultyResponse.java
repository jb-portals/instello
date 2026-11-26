package in.instello.module.faculty.dto;

import in.instello.module.faculty.Faculty;
import org.jspecify.annotations.Nullable;

public record ActivateFacultyResponse(
    String id, String firstName, @Nullable String lastName, Boolean active) {
  public static ActivateFacultyResponse from(Faculty faculty) {
    return new ActivateFacultyResponse(
        faculty.getId(), faculty.getFirstName(), faculty.getLastName(), faculty.isActive());
  }
}
