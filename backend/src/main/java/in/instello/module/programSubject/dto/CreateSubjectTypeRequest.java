package in.instello.module.programSubject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubjectTypeRequest(@NotBlank @Size(max = 60) String name) {}
