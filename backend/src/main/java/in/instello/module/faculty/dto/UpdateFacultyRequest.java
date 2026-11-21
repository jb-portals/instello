package in.instello.module.faculty.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import in.instello.common.address.dto.AddressRequest;
import in.instello.module.faculty.Faculty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record UpdateFacultyRequest(
    @Nullable @Size(max = 30) @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String firstName,
    @Nullable @Size(max = 30) String lastName,
    @Nullable @Past @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
    @Nullable String profilePhoto,
    @Nullable @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String designation,
    @Nullable String qualification,
    @Nullable String specialization,
    @Nullable @JsonAlias("joiningDate") @JsonFormat(pattern = "dd-MM-yyyy") LocalDate joinedDate,
    @Nullable
        @JsonAlias("SecondaryPhoneNumber")
        @Size(max = 15)
        @Pattern(
            regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
        String secondaryPhoneNumber,
    @Nullable @Valid AddressRequest address) {

  public void applyTo(Faculty faculty) {
    if (firstName != null) {
      faculty.setFirstName(firstName);
    }
    if (lastName != null) {
      faculty.setLastName(lastName);
    }
    if (dateOfBirth != null) {
      faculty.setDateOfBirth(dateOfBirth);
    }
    if (profilePhoto != null) {
      faculty.setProfilePhoto(profilePhoto);
    }
    if (designation != null) {
      faculty.setDesignation(designation);
    }
    if (qualification != null) {
      faculty.setQualification(qualification);
    }
    if (specialization != null) {
      faculty.setSpecialization(specialization);
    }
    if (joinedDate != null) {
      faculty.setJoinedDate(joinedDate);
    }
    if (secondaryPhoneNumber != null) {
      faculty.setSecondaryPhoneNumber(secondaryPhoneNumber);
    }
    if (address != null) {
      faculty.setAddress(address.toAddress());
    }
  }
}
