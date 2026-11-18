package in.instello.module.program;

import in.instello.common.exception.response.PageResponse;
import in.instello.module.program.dto.CreateProgramRequest;
import in.instello.module.program.dto.CreateProgramResponse;
import in.instello.module.program.dto.ProgramAliasAvailabilityRequest;
import in.instello.module.program.dto.ProgramAliasAvailabilityResponse;
import in.instello.module.program.dto.ProgramArchiveResponse;
import in.instello.module.program.dto.ProgramResponse;
import in.instello.module.program.dto.UpdateProgramAliasRequest;
import in.instello.module.program.dto.UpdateProgramRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/programs/{institution}")
public class ProgramController {

  private final ProgramService programService;

  public ProgramController(ProgramService programService) {
    this.programService = programService;
  }

  @PostMapping
  @Operation(operationId = "createProgram")
  public ResponseEntity<CreateProgramResponse> create(
      @RequestBody CreateProgramRequest createProgramRequest, @PathVariable String institution) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(programService.create(createProgramRequest, institution));
  }

  @GetMapping("/{program}")
  @Operation(operationId = "showProgram")
  public ResponseEntity<ProgramResponse> show(
      @PathVariable String institution, @PathVariable String program) {
    return ResponseEntity.ok(programService.show(institution, program));
  }

  @PatchMapping("/{program}")
  @Operation(operationId = "updateProgramName")
  public ResponseEntity<ProgramResponse> update(
      @RequestBody @Valid UpdateProgramRequest updateProgramRequest,
      @PathVariable String institution,
      @PathVariable String program) {
    return ResponseEntity.ok(programService.update(updateProgramRequest, institution, program));
  }

  @GetMapping("/aliases/check")
  @Operation(operationId = "checkProgramAlias")
  public ResponseEntity<ProgramAliasAvailabilityResponse> checkAlias(
      @PathVariable String institution,
      @Valid @RequestBody ProgramAliasAvailabilityRequest programAliasAvailabilityRequest) {
    return ResponseEntity.ok(
        programService.checkAlias(institution, programAliasAvailabilityRequest));
  }

  @PatchMapping("/{program}/alias")
  @Operation(operationId = "updateProgramAlias")
  public ResponseEntity<ProgramResponse> updateAlias(
      @RequestBody UpdateProgramAliasRequest updateProgramAliasRequest,
      @PathVariable String institution,
      @PathVariable String program) {
    return ResponseEntity.ok(
        programService.updateAlias(updateProgramAliasRequest, institution, program));
  }

  @PatchMapping("/{program}/archive")
  @Operation(operationId = "archiveProgram")
  public ResponseEntity<ProgramArchiveResponse> archive(
      @PathVariable String institution, @PathVariable String program) {
    return ResponseEntity.ok(programService.archive(institution, program));
  }

  @PatchMapping("/{program}/unarchive")
  @Operation(operationId = "unarchiveProgram")
  public ResponseEntity<ProgramArchiveResponse> unarchive(
      @PathVariable String institution, @PathVariable String program) {
    return ResponseEntity.ok(programService.unarchive(institution, program));
  }

  @GetMapping
  @Operation(operationId = "listPrograms")
  public ResponseEntity<PageResponse<ProgramResponse>> list(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(programService.list(institution, search, pageable));
  }

  @GetMapping("/archive")
  @Operation(operationId = "listArchivedPrograms")
  public ResponseEntity<PageResponse<ProgramResponse>> listArchives(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(programService.listArchives(institution, search, pageable));
  }
}
