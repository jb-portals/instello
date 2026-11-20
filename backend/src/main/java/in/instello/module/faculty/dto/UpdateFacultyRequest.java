package in.instello.module.faculty.dto;

import in.instello.common.address.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record UpdateFacultyRequest(
    @Nullable @Size(max = 30) String firstName,
    @Nullable @Size(max = 30) String lastName,
    @Nullable @Past LocalDate dateOfBirth,
    @Nullable String Designation,
    @Nullable String qualification,
    @Nullable String specialization,
    @Nullable LocalDate joiningDate,
    @Nullable
        @Size(max = 15)
        @Pattern(
            regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
        String SecondaryPhoneNumber,
    @Nullable @Valid AddressRequest address) {}
