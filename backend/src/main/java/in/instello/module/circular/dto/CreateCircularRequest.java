package in.instello.module.circular.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateCircularRequest(
    @NotBlank(message = "Title is required") String title,
    @NotBlank(message = "Content is required") String content,
    @NotNull(message = "Expiry date is required") Instant expiresAt) {}
