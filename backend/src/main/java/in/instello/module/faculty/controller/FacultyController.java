package in.instello.module.faculty.controller;

import in.instello.module.faculty.dto.CreateFacultyRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import in.instello.module.faculty.dto.FacultyResponse;

@RestController
@RequestMapping("/v1/faculty/{institution}")
public class FacultyController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacultyResponse createFaculty(
            @PathVariable String institution,
            @Valid @RequestBody CreateFacultyRequest request) {
    
        // TODO: Business logic will be implemented in Issue #26
        return null;
    }
}