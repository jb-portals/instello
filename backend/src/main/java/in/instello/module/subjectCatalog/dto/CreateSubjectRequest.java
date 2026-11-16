package in.instello.module.subjectCatalog.dto;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public record CreateSubjectRequest(
    @NotNull String name,
    @NotNull String code,
    @NotNull String colorHash,
    @NotNull String alias,
    @Nullable String description) {}
