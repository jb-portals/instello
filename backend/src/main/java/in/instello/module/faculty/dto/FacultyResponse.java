package in.instello.module.faculty.dto;

import in.instello.common.address.dto.AddressResponse;
import in.instello.module.faculty.Faculty;
import java.time.Instant;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record FacultyResponse(
    String id,
    String firstName,
    @Nullable String lastName,
    LocalDate dateOfBirth,
    String email,
    @Nullable String profilePhoto,
    String staffId,
    String designation,
    @Nullable String qualification,
    @Nullable String specialization,
    @Nullable LocalDate joinedDate,
    String primaryPhoneNumber,
    @Nullable String secondaryPhoneNumber,
    AddressResponse address,
    Boolean active,
    @Nullable Instant inactivatedAt,
    Instant createdAt) {
  public static FacultyResponse from(Faculty faculty) {
    AddressResponse addressResponse = AddressResponse.from(faculty.getAddress());

    return new FacultyResponse(
        faculty.getId(),
        faculty.getFirstName(),
        faculty.getLastName(),
        faculty.getDateOfBirth(),
        faculty.getEmail(),
        faculty.getProfilePhoto(),
        faculty.getStaffId(),
        faculty.getDesignation(),
        faculty.getQualification(),
        faculty.getSpecialization(),
        faculty.getJoinedDate(),
        faculty.getPrimaryPhoneNumber(),
        faculty.getSecondaryPhoneNumber(),
        addressResponse,
        faculty.isActive(),
        faculty.getInactivatedAt(),
        faculty.getCreatedAt());
  }
}
