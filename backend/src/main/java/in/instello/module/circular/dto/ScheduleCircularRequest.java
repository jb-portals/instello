package in.instello.module.circular.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ScheduleCircularRequest(
    @NotNull(message = "Scheduled date is required")
    @Future(message = "Scheduled date must be in the future")
    Instant scheduledAt
) {}