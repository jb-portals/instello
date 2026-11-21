package in.instello.module.faculty.dto;

import in.instello.module.faculty.Faculty;

public record CreateFacultyResponse(String id, String firstName, String lastName, String staffId) {
  public static CreateFacultyResponse fromFaculty(Faculty faculty) {
    return new CreateFacultyResponse(
        faculty.getId(), faculty.getFirstName(), faculty.getLastName(), faculty.getStaffId());
  }
}
