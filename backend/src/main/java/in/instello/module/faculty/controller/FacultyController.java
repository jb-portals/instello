
package in.instello.module.faculty.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import in.instello.module.faculty.service.FacultyBulkImportService;

@RestController
@RequestMapping("/v1/faculty/{institution}")
public class FacultyController {

    private final FacultyBulkImportService facultyBulkImportService;

    public FacultyController(
            FacultyBulkImportService facultyBulkImportService) {
        this.facultyBulkImportService = facultyBulkImportService;
    }

    /**
     * Uploads a CSV or Excel file containing faculty details.
     *
     * Required columns:
     * First Name, Last Name, Date of Birth, Email, Staff ID,
     * Designation, Qualification, Specialization,
     * Primary Phone Number, Joined Date.
     */
    @PostMapping("/bulk-import")
    public ResponseEntity<?> uploadFacultyFile(
            @PathVariable String institution,
            @RequestParam("file") MultipartFile file) {

        try {
            List<String> missingColumns =
                    facultyBulkImportService.validateFile(file);

            if (!missingColumns.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Missing required columns: " + missingColumns);
            }

            return ResponseEntity.accepted()
                    .body("File format and column validation successful.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Unable to read the uploaded file.");
        }
    }
}
