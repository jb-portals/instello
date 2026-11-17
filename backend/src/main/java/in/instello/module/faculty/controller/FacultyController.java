
package in.instello.module.faculty.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/faculty/{institution}")
public class FacultyController {

    @PostMapping("/bulk-import")
    public ResponseEntity<String> uploadFacultyFile(
            @PathVariable String institution,
            @RequestParam("file") MultipartFile file) {

        // Business logic will be implemented in Issue #26
        return ResponseEntity.accepted()
                .body("Bulk import request received");
    }
}
