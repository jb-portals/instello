package in.instello.module.faculty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFacultyStaffIdRequest(@NotBlank @Size(max = 15) String staffId) {}
