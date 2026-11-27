package in.instello.module.programSubject.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record AddProgramSubjectRequest(
    @NotBlank String subject, @NotBlank String subjectType, @Nullable String description) {}
