package in.instello.module.faculty.dto;

import org.jspecify.annotations.Nullable;

public record UpdateFacultyStaffIdResponse(
    String id, String firstName, @Nullable String lastName, String staffId) {}
