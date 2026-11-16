package in.instello.module.subjectCatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record UpdateSubjectRequest(
    @Nullable @NotBlank @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String name,
    @Nullable @NotBlank @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String code,
    @Nullable @NotBlank @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String colorHash,
    @Nullable @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String description) {}
