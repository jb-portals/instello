package in.instello.module.faculty.dto;

import in.instello.module.faculty.Faculty;
import org.jspecify.annotations.Nullable;

public record CreateFacultyResponse(
    String id, String firstName, @Nullable String lastName, String staffId) {
  public static CreateFacultyResponse fromFaculty(Faculty faculty) {
    return new CreateFacultyResponse(
        faculty.getId(), faculty.getFirstName(), faculty.getLastName(), faculty.getStaffId());
  }
}
