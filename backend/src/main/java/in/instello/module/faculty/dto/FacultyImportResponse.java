package in.instello.module.faculty.dto;

import java.util.List;

public record FacultyImportResponse(
    int totalRecords,
    int successfulRecords,
    int failedRecords,
    List<FacultyImportRowError> errors) {}
