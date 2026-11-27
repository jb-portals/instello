package in.instello.module.academicPattern;

import in.instello.config.openApi.annotation.CommonErrorResponses;
import in.instello.module.academicPattern.dto.AcademicPatternResponse;
import in.instello.module.academicPattern.dto.CreateAcademicPatternRequest;
import in.instello.module.academicPattern.dto.UpdateAcademicPatternRequest;
import in.instello.module.academicPattern.service.AcademicPatternService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/academic-patterns/{organizationId}")
@Tag(name = "academicPatterns")
@Validated
@CommonErrorResponses
public class AcademicPatternController {

  private final AcademicPatternService academicPatternService;

  public AcademicPatternController(AcademicPatternService academicPatternService) {
    this.academicPatternService = academicPatternService;
  }

  @GetMapping
  @Operation(operationId = "listAcademicPatterns")
  public ResponseEntity<List<AcademicPatternResponse>> list(
      @PathVariable String organizationId, Authentication authentication) {
    return ResponseEntity.ok(academicPatternService.list(organizationId, authentication.getName()));
  }

  @GetMapping("/{patternId}")
  @Operation(operationId = "getAcademicPattern")
  public ResponseEntity<AcademicPatternResponse> get(
      @PathVariable String organizationId,
      @PathVariable String patternId,
      Authentication authentication) {
    return ResponseEntity.ok(
        academicPatternService.get(organizationId, patternId, authentication.getName()));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(operationId = "createAcademicPattern")
  public ResponseEntity<AcademicPatternResponse> create(
      @PathVariable String organizationId,
      @Valid @RequestBody CreateAcademicPatternRequest requestBody,
      Authentication authentication) {
    AcademicPatternResponse created =
        academicPatternService.create(organizationId, requestBody, authentication.getName());
    return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequestUri()
                .pathSegment(created.id())
                .build()
                .toUri())
        .body(created);
  }

  @DeleteMapping("/{patternId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(operationId = "deleteAcademicPattern")
  public ResponseEntity<Void> delete(
      @PathVariable String organizationId,
      @PathVariable String patternId,
      Authentication authentication) {
    academicPatternService.delete(organizationId, patternId, authentication.getName());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{patternId}")
  @Operation(operationId = "updateAcademicPattern")
  public ResponseEntity<AcademicPatternResponse> update(
      @PathVariable String organizationId,
      @PathVariable String patternId,
      @Valid @RequestBody UpdateAcademicPatternRequest requestBody,
      Authentication authentication) {
    return ResponseEntity.ok(
        academicPatternService.update(
            organizationId, patternId, requestBody, authentication.getName()));
  }
}
