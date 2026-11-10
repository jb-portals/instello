package in.instello.module.accessRequest.dto;

import in.instello.module.accessRequest.entity.AccessRequestStatus;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateAccessRequestRequest(
    @Nullable AccessRequestStatus status, @Nullable @Size(max = 5000) String adminNotes) {}
