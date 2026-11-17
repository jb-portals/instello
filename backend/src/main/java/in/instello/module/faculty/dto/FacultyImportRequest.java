package in.instello.module.faculty.dto;

import org.springframework.web.multipart.MultipartFile;

public record FacultyImportRequest(MultipartFile file) {}
