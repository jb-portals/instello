package in.instello.module.program.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProgramRequest(@NotBlank String name, @NotBlank String alias) {}
