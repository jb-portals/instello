package in.instello.module.program.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProgramRequest(@NotBlank String name) {}
