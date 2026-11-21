package in.instello.module.faculty.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import in.instello.common.address.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public class UpdateFacultyRequest {
  @Size(max = 30)
  @Nullable
  private String firstName;

  @Size(max = 30)
  @Nullable
  private String lastName;

  @Past @Nullable private LocalDate dateOfBirth;

  @Nullable private String profilePhoto;

  @Nullable private String designation;

  @Nullable private String qualification;

  @Nullable private String specialization;

  @Nullable private LocalDate joiningDate;

  @Size(max = 15)
  @Pattern(
      regexp = "^(?:(?:\\+91|91|0)[ -]?)?[6-9]\\d{9}$",
      message = "Enter a valid Indian mobile number")
  @Nullable
  private String secondaryPhoneNumber;

  @Valid @Nullable private AddressRequest address;

  @JsonIgnore private boolean lastNameProvided;
  @JsonIgnore private boolean profilePhotoProvided;
  @JsonIgnore private boolean qualificationProvided;
  @JsonIgnore private boolean specializationProvided;
  @JsonIgnore private boolean joiningDateProvided;
  @JsonIgnore private boolean secondaryPhoneNumberProvided;

  @JsonSetter(nulls = Nulls.FAIL)
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @JsonSetter
  public void setLastName(@Nullable String lastName) {
    this.lastName = lastName;
    this.lastNameProvided = true;
  }

  @JsonSetter(nulls = Nulls.FAIL)
  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  @JsonSetter
  public void setProfilePhoto(@Nullable String profilePhoto) {
    this.profilePhoto = profilePhoto;
    this.profilePhotoProvided = true;
  }

  @JsonSetter(nulls = Nulls.FAIL)
  public void setDesignation(String designation) {
    this.designation = designation;
  }

  @JsonSetter
  public void setQualification(@Nullable String qualification) {
    this.qualification = qualification;
    this.qualificationProvided = true;
  }

  @JsonSetter
  public void setSpecialization(@Nullable String specialization) {
    this.specialization = specialization;
    this.specializationProvided = true;
  }

  @JsonSetter
  public void setJoiningDate(@Nullable LocalDate joiningDate) {
    this.joiningDate = joiningDate;
    this.joiningDateProvided = true;
  }

  @JsonSetter("SecondaryPhoneNumber")
  public void setSecondaryPhoneNumber(@Nullable String secondaryPhoneNumber) {
    this.secondaryPhoneNumber = secondaryPhoneNumber;
    this.secondaryPhoneNumberProvided = true;
  }

  @JsonSetter(nulls = Nulls.FAIL)
  public void setAddress(AddressRequest address) {
    this.address = address;
  }
}
