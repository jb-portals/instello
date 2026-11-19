package in.instello.module.program.dto;

import jakarta.validation.constraints.NotBlank;

public record ProgramAliasAvailabilityRequest(@NotBlank String alias) {}
