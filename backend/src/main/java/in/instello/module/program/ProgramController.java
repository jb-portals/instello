package in.instello.module.program;

import in.instello.common.exception.response.PageResponse;
import in.instello.module.program.dto.CreateProgramRequest;
import in.instello.module.program.dto.ProgramAliasAvailabilityResponse;
import in.instello.module.program.dto.ProgramArchiveResponse;
import in.instello.module.program.dto.ProgramResponse;
import in.instello.module.program.dto.UpdateProgramAliasRequest;
import in.instello.module.program.dto.UpdateProgramRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/programs/{institution}")
public class ProgramController {

  @PostMapping
  @Operation(operationId = "createProgram")
  public ResponseEntity<ProgramResponse> create(
      @RequestBody CreateProgramRequest createProgramRequest, @PathVariable String institution) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/{program}")
  @Operation(operationId = "showProgram")
  public ResponseEntity<ProgramResponse> show(
      @PathVariable String institution, @PathVariable String program) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("/{program}")
  @Operation(operationId = "updateProgramName")
  public ResponseEntity<ProgramResponse> update(
      @RequestBody UpdateProgramRequest updateProgramRequest,
      @PathVariable String institution,
      @PathVariable String program) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/aliases/check")
  @Operation(operationId = "checkProgramAlias")
  public ResponseEntity<ProgramAliasAvailabilityResponse> checkAlias(
      @PathVariable String institution, @RequestParam String alias) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PatchMapping("/{program}/alias")
  @Operation(operationId = "updateProgramAlias")
  public ResponseEntity<ProgramResponse> updateAlias(
      @RequestBody UpdateProgramAliasRequest updateProgramAliasRequest,
      @PathVariable String institution,
      @PathVariable String program) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PostMapping("/{program}/archive")
  @Operation(operationId = "archiveProgram")
  public ResponseEntity<ProgramArchiveResponse> archive(
      @PathVariable String institution, @PathVariable String program) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PostMapping("/{program}/unarchive")
  @Operation(operationId = "unarchiveProgram")
  public ResponseEntity<ProgramArchiveResponse> unarchive(
      @PathVariable String institution, @PathVariable String program) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping
  @Operation(operationId = "listPrograms")
  public ResponseEntity<PageResponse<ProgramResponse>> list(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/archive")
  @Operation(operationId = "listArchivedPrograms")
  public ResponseEntity<PageResponse<ProgramResponse>> listArchives(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
