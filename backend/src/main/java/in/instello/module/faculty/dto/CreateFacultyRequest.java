package in.instello.module.faculty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateFacultyRequest(

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @NotNull
    LocalDate dateOfBirth,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String staffId,

    @NotBlank
    String designation,

    @NotBlank
    String qualification,

    @NotBlank
    String specialization,

    @NotNull
    LocalDate joinedDate,

    @NotBlank
    String primaryPhoneNumber,

    String secondaryPhoneNumber,

    String profilePhoto

) {}