package in.instello.module.faculty.controller;

import in.instello.common.exception.response.PageResponse;
import in.instello.module.faculty.dto.CreateFacultyRequest;
import in.instello.module.faculty.dto.FacultyImportResponse;
import in.instello.module.faculty.dto.FacultyResponse;
import in.instello.module.faculty.dto.UpdateFacultyRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/faculty/{institution}")
public class FacultyController {

  @PostMapping
  @Operation(operationId = "createFaculty")
  public ResponseEntity<FacultyResponse> create(
      @RequestBody CreateFacultyRequest createFacultyRequest, @PathVariable String institution) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("/{faculty}")
  @Operation(operationId = "updateFaculty")
  public ResponseEntity<FacultyResponse> update(
      @RequestBody UpdateFacultyRequest updateFacultyRequest,
      @PathVariable String institution,
      @PathVariable String faculty) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @DeleteMapping("/{faculty}")
  @Operation(operationId = "deleteFaculty")
  public ResponseEntity<Void> delete(
      @PathVariable String institution, @PathVariable String faculty) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @GetMapping
  @Operation(operationId = "listFaculty")
  public ResponseEntity<PageResponse<FacultyResponse>> list(
      @PathVariable String institution,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/{faculty}")
  @Operation(operationId = "showFaculty")
  public ResponseEntity<FacultyResponse> show(
      @PathVariable String institution, @PathVariable String faculty) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PostMapping("/bulk-import")
  @Operation(operationId = "bulkImportFaculty")
  public ResponseEntity<FacultyImportResponse> bulkImport(
      @PathVariable String institution, @RequestParam("file") MultipartFile file) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
