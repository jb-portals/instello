package in.instello.module.programSubject;

import in.instello.common.responses.PageResponse;
import in.instello.module.programSubject.dto.*;
import in.instello.module.programSubject.service.ProgramSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "ProgramSubject")
@RequestMapping("/program-subjects/{institution}/{program}/{academicStage}")
public class ProgramSubjectController {

  private final ProgramSubjectService programSubjectService;

  public ProgramSubjectController(ProgramSubjectService programSubjectService) {
    this.programSubjectService = programSubjectService;
  }

  @PostMapping("/{program}/{academicStage}")
  @Operation(operationId = "addSubject")
  public ResponseEntity<AddProgramSubjectResponse> add(
      @RequestBody @Valid AddProgramSubjectRequest addProgramSubjectRequest,
      @PathVariable String institution,
      @PathVariable String program,
      @PathVariable String academicStage) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            programSubjectService.add(
                addProgramSubjectRequest, institution, program, academicStage));
  }

  @GetMapping("/subject-types")
  @Operation(operationId = "listSubjectTypes")
  public ResponseEntity<PageResponse<SubjectTypeResponse>> list(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    // return ResponseEntity.ok(programSubjectService.listSubjectTypes(institution, search,
    // pageable));
  }

  @GetMapping("/subject-types/{subjectType}")
  @Operation(operationId = "showSubjectType")
  public ResponseEntity<SubjectTypeResponse> show(
      @PathVariable String institution, @PathVariable String subjectType) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    // return ResponseEntity.ok(programSubjectService.show(institution, subjectType));
  }

  @PatchMapping("/subject-types/{subjectType}")
  @Operation(operationId = "updateName")
  public ResponseEntity<SubjectTypeResponse> updateName(
      @RequestBody @Valid UpdateSubjectTypeNameRequest updateSubjectTypeNameRequest,
      @PathVariable String institution,
      @PathVariable String subjectType) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    // return ResponseEntity.ok(programSubjectService.updateName(updateSubjectTypeNameRequest,
    // institution, subjectType));
  }

  @PatchMapping("/subject-types/{subjectType}/archive")
  @Operation(operationId = "archiveSubjectType")
  public ResponseEntity<SubjectTypeArchiveResponse> archiveSubjectType(
      @PathVariable String institution, @PathVariable String subjectType) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    // return ResponseEntity.ok(programSubjectService.archiveSubjectType(institution, subjectType));
  }

  @PatchMapping("/subject-types/{subjectType}/unarchive")
  @Operation(operationId = "unarchiveSubjectType")
  public ResponseEntity<SubjectTypeArchiveResponse> unarchiveSubjectType(
      @PathVariable String institution, @PathVariable String subjectType) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    // return ResponseEntity.ok(programSubjectService.unarchiveSubjectType(institution,
    // subjectType));
  }

  @GetMapping("/subject-types/archived")
  @Operation(operationId = "listArchivedSubjectTypes")
  public ResponseEntity<PageResponse<SubjectTypeResponse>> listArchivedSubjectTypes(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    // return ResponseEntity.ok( programSubjectService.listArchivedSubjectTypes(institution, search,
    // pageable));
  }
}
