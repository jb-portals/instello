package in.instello.module.subjectCatalog.dto;

import jakarta.validation.constraints.NotBlank;

public record SubjectAliasAvailabilityRequest(@NotBlank String alias) {}
