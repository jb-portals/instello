package in.instello.module.faculty.dto;

import java.time.Instant;
import java.time.LocalDate;

public record FacultyResponse(
    String staffRecordId,
    String institutionId,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String email,
    String staffId,
    String designation,
    String qualification,
    String specialization,
    LocalDate joinedDate,
    String primaryPhoneNumber,
    String secondaryPhoneNumber,
    String profilePhoto,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {}
