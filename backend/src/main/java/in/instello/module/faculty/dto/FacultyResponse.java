package in.instello.module.faculty.dto;

import java.util.Date;
import org.jspecify.annotations.Nullable;

public record FacultyResponse(
    String id,
    String firstName,
    @Nullable String lastName,
    Date dateOfBirth,
    String email,
    @Nullable String profilePhoto,
    String staffId,
    String Designation,
    String qualification,
    String specialization,
    @Nullable Date joiningDate,
    String primaryPhoneNumber,
    @Nullable String SecondaryPhoneNumber,
    String address,
    Boolean active,
    @Nullable Date inactivatedAt) {}
