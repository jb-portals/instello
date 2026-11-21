package in.instello.module.faculty.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.instello.common.address.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record CreateFacultyRequest(
    @NotBlank @Size(max = 30) String firstName,
    @NotBlank @Size(max = 30) String lastName,
    @NotNull @Past @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
    @NotBlank @Email String email,
    @NotBlank @Size(max = 15) String staffId,
    @Nullable String profilePhoto,
    @NotBlank String designation,
    @NotBlank String qualification,
    @NotBlank String specialization,
    @NotNull @JsonFormat(pattern = "dd-MM-yyyy") LocalDate joinedDate,
    @NotBlank
        @Size(max = 15)
        @Pattern(
            regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
        String primaryPhoneNumber,
    @NotBlank
        @Size(max = 15)
        @Pattern(
            regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
        String SecondaryPhoneNumber,
    @NotNull @Valid AddressRequest address) {}
