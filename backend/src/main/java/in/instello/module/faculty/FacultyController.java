package in.instello.module.faculty;

import in.instello.common.responses.PageResponse;
import in.instello.module.faculty.dto.ActivateFacultyResponse;
import in.instello.module.faculty.dto.CheckEmailRequest;
import in.instello.module.faculty.dto.CheckPrimaryPhoneNumberRequest;
import in.instello.module.faculty.dto.CheckStaffIdRequest;
import in.instello.module.faculty.dto.CreateFacultyRequest;
import in.instello.module.faculty.dto.CreateFacultyResponse;
import in.instello.module.faculty.dto.FacultyAvailabilityResponse;
import in.instello.module.faculty.dto.FacultyResponse;
import in.instello.module.faculty.dto.UpdateFacultyRequest;
import in.instello.module.faculty.dto.UpdateFacultyStaffIdRequest;
import in.instello.module.faculty.dto.UpdateFacultyStaffIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Faculty")
@RequestMapping("faculty/{institution}")
@Validated
public class FacultyController {

  private final FacultyService facultyService;

  FacultyController(FacultyService facultyService) {
    this.facultyService = facultyService;
  }

  @PostMapping
  @Operation(operationId = "createFaculty")
  public ResponseEntity<CreateFacultyResponse> create(
      @RequestBody @Valid CreateFacultyRequest createFacultyRequest,
      @PathVariable String institution) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(facultyService.create(createFacultyRequest, institution));
  }

  @GetMapping
  @Operation(operationId = "listFaculty")
  public ResponseEntity<PageResponse<FacultyResponse>> list(
      @PathVariable String institution,
      @RequestParam(required = false) @Nullable String search,
      @PageableDefault(size = 10, sort = "staffId", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return ResponseEntity.ok(facultyService.list(institution, search, pageable));
  }

  @GetMapping("/{staff}")
  @Operation(operationId = "showFaculty")
  public ResponseEntity<FacultyResponse> show(
      @PathVariable String institution, @PathVariable String staff) {
    return ResponseEntity.ok(facultyService.show(institution, staff));
  }

  @GetMapping("staff-id/check")
  @Operation(operationId = "checkStaffId")
  public ResponseEntity<FacultyAvailabilityResponse> checkStaffId(
      @PathVariable String institution,
      @Valid @RequestBody CheckStaffIdRequest checkStaffIdRequest) {
    return ResponseEntity.ok(facultyService.checkStaffId(institution, checkStaffIdRequest));
  }

  @GetMapping("email/check")
  @Operation(operationId = "checkEmail")
  public ResponseEntity<FacultyAvailabilityResponse> checkEmail(
      @PathVariable String institution, @Valid @RequestBody CheckEmailRequest checkEmailRequest) {
    return ResponseEntity.ok(facultyService.checkEmail(institution, checkEmailRequest));
  }

  @GetMapping("primary-phone-number/check")
  @Operation(operationId = "checkPrimaryPhoneNumber")
  public ResponseEntity<FacultyAvailabilityResponse> checkPrimaryPhoneNumber(
      @PathVariable String institution,
      @Valid @RequestBody CheckPrimaryPhoneNumberRequest checkPrimaryPhoneNumberRequest) {
    return ResponseEntity.ok(
        facultyService.checkPrimaryPhoneNumber(institution, checkPrimaryPhoneNumberRequest));
  }

  @PatchMapping("/{staff}")
  @Operation(operationId = "updateFaculty")
  public ResponseEntity<FacultyResponse> update(
      @RequestBody @Valid UpdateFacultyRequest updateFacultyRequest,
      @PathVariable String institution,
      @PathVariable("staff") String staffId) {
    return ResponseEntity.ok(facultyService.update(institution, staffId, updateFacultyRequest));
  }

  @PatchMapping("/{staff}/staffid")
  @Operation(operationId = "updateStaffId")
  public ResponseEntity<UpdateFacultyStaffIdResponse> updateStaffId(
      @PathVariable String institution,
      @PathVariable("staff") String facultyId,
      @RequestBody @Valid UpdateFacultyStaffIdRequest updateFacultyStaffIdRequest) {
    return ResponseEntity.ok(
        facultyService.updateStaffId(institution, facultyId, updateFacultyStaffIdRequest));
  }

  @PatchMapping("/{staff}/deactivate")
  @Operation(operationId = "deacticateStaff")
  public ResponseEntity<ActivateFacultyResponse> deactivate(
      @PathVariable String institution, @PathVariable("staff") String staffId) {
    return ResponseEntity.ok(facultyService.deactivate(institution, staffId));
  }

  @GetMapping("/inactive")
  @Operation(operationId = "listInactiveFaculty")
  public ResponseEntity<PageResponse<FacultyResponse>> listInactive(
      @PathVariable String institution,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "inactivatedAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(facultyService.listInactive(institution, search, pageable));
  }

  @PatchMapping("/{staff}/activate")
  @Operation(operationId = "acticateStaff")
  public ResponseEntity<ActivateFacultyResponse> activate(
      @PathVariable String institution, @PathVariable("staff") String staffId) {
    return ResponseEntity.ok(facultyService.activate(institution, staffId));
  }
}
