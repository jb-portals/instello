package in.instello.module.subjectCatalog;

import in.instello.common.exception.response.PageResponse;
import in.instello.module.subjectCatalog.dto.CreateSubjectRequest;
import in.instello.module.subjectCatalog.dto.CreateSubjectResponse;
import in.instello.module.subjectCatalog.dto.SubjectArchiveResponse;
import in.instello.module.subjectCatalog.dto.SubjectResponse;
import in.instello.module.subjectCatalog.dto.UpdateSubjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subjects-catalog/{institution}")
public class SubjectCatalogController {

  @PostMapping
  public ResponseEntity<CreateSubjectResponse> create(
      @RequestBody CreateSubjectRequest createSubjectRequest, @RequestParam String institution) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("{subject}")
  public ResponseEntity<SubjectResponse> update(
      @RequestBody UpdateSubjectRequest updateSubjectRequest,
      @RequestParam String institution,
      @RequestParam String subject) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("{subject}/archive")
  public ResponseEntity<SubjectArchiveResponse> archive(
      @RequestParam String institution, @RequestParam String subject) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("{subject}/unarchive")
  public ResponseEntity<SubjectArchiveResponse> unarchive(
      @RequestParam String institution, @RequestParam String subject) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping
  @Operation(operationId = "listSubjects")
  public ResponseEntity<PageResponse<SubjectResponse>> list(
      @RequestParam String institution,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("{subject}")
  @Operation(operationId = "showSubject")
  public ResponseEntity<SubjectResponse> show(
      @RequestParam String institution, @RequestParam String subject) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("archive")
  public ResponseEntity<PageResponse<SubjectResponse>> listArchives(
      @RequestParam String institution,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
