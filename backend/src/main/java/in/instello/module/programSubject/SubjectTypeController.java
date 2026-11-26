package in.instello.module.programSubject;

import in.instello.common.responses.PageResponse;
import in.instello.module.programSubject.dto.CreateSubjectTypeRequest;
import in.instello.module.programSubject.dto.CreateSubjectTypeResponse;
import in.instello.module.programSubject.dto.SubjectTypeArchiveResponse;
import in.instello.module.programSubject.dto.SubjectTypeResponse;
import in.instello.module.programSubject.dto.UpdateSubjectTypeNameRequest;
import in.instello.module.programSubject.service.SubjectTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "subjectType")
@RequestMapping("/program-subjects/{institution}")
public class SubjectTypeController {

  private final SubjectTypeService subjectTypeService;

  public SubjectTypeController(SubjectTypeService subjectTypeService) {
    this.subjectTypeService = subjectTypeService;
  }

  @PostMapping("/subject-types")
  @Operation(operationId = "createSubjectType")
  public ResponseEntity<CreateSubjectTypeResponse> createSubjectType(
      @RequestBody @Valid CreateSubjectTypeRequest createSubjectTypeRequest,
      @PathVariable String institution) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(subjectTypeService.createSubjectType(createSubjectTypeRequest, institution));
  }

  @GetMapping("/subject-types")
  @Operation(operationId = "listSubjectTypes")
  public ResponseEntity<PageResponse<SubjectTypeResponse>> listSubjectTypes(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(subjectTypeService.listSubjectTypes(institution, search, pageable));
  }

  @GetMapping("/subject-types/{subjectType}")
  @Operation(operationId = "showSubjectType")
  public ResponseEntity<SubjectTypeResponse> show(
      @PathVariable String institution, @PathVariable String subjectType) {
    return ResponseEntity.ok(subjectTypeService.show(institution, subjectType));
  }

  @PatchMapping("/subject-types/{subjectType}")
  @Operation(operationId = "updateName")
  public ResponseEntity<SubjectTypeResponse> updateName(
      @RequestBody @Valid UpdateSubjectTypeNameRequest updateSubjectTypeNameRequest,
      @PathVariable String institution,
      @PathVariable String subjectType) {
    return ResponseEntity.ok(
        subjectTypeService.updateName(updateSubjectTypeNameRequest, institution, subjectType));
  }

  @PatchMapping("/subject-types/{subjectType}/archive")
  @Operation(operationId = "archiveSubjectType")
  public ResponseEntity<SubjectTypeArchiveResponse> archiveSubjectType(
      @PathVariable String institution, @PathVariable String subjectType) {
    return ResponseEntity.ok(subjectTypeService.archiveSubjectType(institution, subjectType));
  }

  @PatchMapping("/subject-types/{subjectType}/unarchive")
  @Operation(operationId = "unarchiveSubjectType")
  public ResponseEntity<SubjectTypeArchiveResponse> unarchiveSubjectType(
      @PathVariable String institution, @PathVariable String subjectType) {
    return ResponseEntity.ok(subjectTypeService.unarchiveSubjectType(institution, subjectType));
  }

  @GetMapping("/subject-types/archived")
  @Operation(operationId = "listArchivedSubjectTypes")
  public ResponseEntity<PageResponse<SubjectTypeResponse>> listArchivedSubjectTypes(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(
        subjectTypeService.listArchivedSubjectTypes(institution, search, pageable));
  }
}
