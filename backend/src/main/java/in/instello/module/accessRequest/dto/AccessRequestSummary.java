package in.instello.module.accessRequest.dto;

import in.instello.module.accessRequest.entity.AccessRequest;
import in.instello.module.accessRequest.entity.AccessRequestStatus;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record AccessRequestSummary(
    String id,
    String organizationName,
    String firstName,
    String lastName,
    String email,
    String phone,
    @Nullable String city,
    @Nullable String state,
    AccessRequestStatus status,
    Instant createdAt) {

  public static AccessRequestSummary from(AccessRequest request) {
    return new AccessRequestSummary(
        request.getId(),
        request.getOrganizationName(),
        request.getFirstName(),
        request.getLastName(),
        request.getEmail(),
        request.getPhone(),
        request.getAddress() != null ? request.getAddress().getCity() : null,
        request.getAddress() != null ? request.getAddress().getState() : null,
        request.getStatus(),
        request.getCreatedAt());
  }
}
