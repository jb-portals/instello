package in.instello.module.subjectCatalog;

import in.instello.common.exception.response.PageResponse;
import in.instello.module.subjectCatalog.dto.CreateSubjectRequest;
import in.instello.module.subjectCatalog.dto.CreateSubjectResponse;
import in.instello.module.subjectCatalog.dto.SubjectAliasAvailabilityRequest;
import in.instello.module.subjectCatalog.dto.SubjectAliasAvailabilityResponse;
import in.instello.module.subjectCatalog.dto.SubjectArchiveResponse;
import in.instello.module.subjectCatalog.dto.SubjectResponse;
import in.instello.module.subjectCatalog.dto.UpdateSubjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "SubjectsCatalog")
@RequestMapping("subjects-catalog/{institution}")
public class SubjectCatalogController {

  private final SubjectCatalogService subjectCatalogService;

  public SubjectCatalogController(SubjectCatalogService subjectCatalogService) {
    this.subjectCatalogService = subjectCatalogService;
  }

  @PostMapping
  @Operation(operationId = "createSubject")
  public ResponseEntity<CreateSubjectResponse> create(
      @RequestBody @Valid CreateSubjectRequest createSubjectRequest,
      @PathVariable String institution) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(subjectCatalogService.create(createSubjectRequest, institution));
  }

  @GetMapping("/aliases/check")
  @Operation(operationId = "checkSubjectAlias")
  public ResponseEntity<SubjectAliasAvailabilityResponse> checkAlias(
      @PathVariable String institution,
      @Valid @RequestBody SubjectAliasAvailabilityRequest subjectAliasAvailabilityRequest) {
    return ResponseEntity.ok(
        subjectCatalogService.checkAlias(institution, subjectAliasAvailabilityRequest));
  }

  @PatchMapping("{subject}")
  @Operation(operationId = "updateSubject")
  public ResponseEntity<SubjectResponse> update(
      @RequestBody @Valid UpdateSubjectRequest updateSubjectRequest,
      @PathVariable String institution,
      @PathVariable String subject) {
    return ResponseEntity.ok(
        subjectCatalogService.update(updateSubjectRequest, institution, subject));
  }

  @PatchMapping("{subject}/archive")
  @Operation(operationId = "archiveSubject")
  public ResponseEntity<SubjectArchiveResponse> archive(
      @PathVariable String institution, @PathVariable String subject) {
    return ResponseEntity.ok(subjectCatalogService.archive(institution, subject));
  }

  @PatchMapping("{subject}/unarchive")
  @Operation(operationId = "unarchiveSubject")
  public ResponseEntity<SubjectArchiveResponse> unarchive(
      @PathVariable String institution, @PathVariable String subject) {
    return ResponseEntity.ok(subjectCatalogService.unarchive(institution, subject));
  }

  @GetMapping
  @Operation(operationId = "listSubjects")
  public ResponseEntity<PageResponse<SubjectResponse>> list(
      @PathVariable String institution,
      @RequestParam(required = false) @Nullable String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(subjectCatalogService.list(institution, search, pageable));
  }

  @GetMapping("{subject}")
  @Operation(operationId = "showSubject")
  public ResponseEntity<SubjectResponse> show(
      @PathVariable String institution, @PathVariable String subject) {
    return ResponseEntity.ok(subjectCatalogService.show(institution, subject));
  }

  @GetMapping("archive")
  @Operation(operationId = "listArchivedSubjects")
  public ResponseEntity<PageResponse<SubjectResponse>> listArchives(
      @PathVariable String institution,
      @RequestParam(required = false) @Nullable String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(subjectCatalogService.listArchives(institution, search, pageable));
  }
}
