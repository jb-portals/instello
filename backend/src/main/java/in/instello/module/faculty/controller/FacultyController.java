
package in.instello.module.faculty.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/faculty/{institution}")
public class FacultyController {

    /**
 * Uploads a CSV or Excel file containing faculty details.
 *
 * Required columns:
 * First Name, Last Name, Date of Birth, Email, Staff ID,
 * Designation, Qualification, Specialization, Primary Phone Number, Joined Date.
 *
 *
 * The uploaded file must contain all required column headers.
 * If any required column is missing or the file format is invalid,
 * the API should return a validation error.
 *
 */
    @PostMapping("/bulk-import")
    public ResponseEntity<String> uploadFacultyFile(
            @PathVariable String institution,
            @RequestParam("file") MultipartFile file) {

        // Business logic will be implemented in Issue #26
        return ResponseEntity.accepted()
                .body("Bulk import request received");
    }
}
