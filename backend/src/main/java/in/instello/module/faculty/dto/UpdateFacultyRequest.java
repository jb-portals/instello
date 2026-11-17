package in.instello.module.faculty.dto;

import jakarta.validation.constraints.Email;
import java.time.LocalDate;

public record UpdateFacultyRequest(
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    @Email String email,
    String designation,
    String qualification,
    String specialization,
    LocalDate joinedDate,
    String primaryPhoneNumber,
    String secondaryPhoneNumber,
    String profilePhoto) {}
