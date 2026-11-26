package in.instello.module.programSubject;

import in.instello.module.programSubject.service.ProgramSubjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "ProgramSubject")
@RequestMapping("/program-subjects/{institution}")
public class ProgramSubjectController {

  private final ProgramSubjectService programSubjectService;

  public ProgramSubjectController(ProgramSubjectService programSubjectService) {
    this.programSubjectService = programSubjectService;
  }

  /*
      @PostMapping ("/{program}/{academicStage}")
  @Operation (operationId = "addSubject")
  public ResponseEntity<AddProgramSubjectResponse> add(
      @RequestBody @Valid AddProgramSubjectRequest addSubjectRequest,
      @PathVariable String institution, @PathVariable String program, @PathVariable String subject, @PathVariable String academicStage, @PathVariable String subject_type  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(programSubjectService.add(addSubjectRequest, institution, program, academicStage));
  }

   */

}
