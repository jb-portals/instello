package in.instello.module.faculty.dto;

import in.instello.common.address.dto.AddressRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record CreateFacultyRequest(
    @NotBlank @Size(max = 30) String firstName,
    @Nullable @Size(max = 30) String lastName,
    @NotBlank @Past LocalDate dateOfBirth,
    @NotBlank @Email String email,
    @NotBlank @Size(max = 15) String staffId,
    @NotBlank String Designation,
    @Nullable String qualification,
    @Nullable String specialization,
    @Nullable LocalDate joiningDate,
    @NotBlank
        @Size(max = 15)
        @Pattern(
            regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
        String primaryPhoneNumber,
    @Nullable
        @Size(max = 15)
        @Pattern(
            regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
        String SecondaryPhoneNumber,
    @NotBlank AddressRequest address) {}
