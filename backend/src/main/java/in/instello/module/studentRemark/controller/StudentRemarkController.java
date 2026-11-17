package in.instello.module.studentRemark.controller;

import in.instello.module.studentRemark.dto.CreateStudentRemarkRequest;
import in.instello.module.studentRemark.dto.StudentRemarkResponse;
import in.instello.module.studentRemark.dto.UpdateStudentRemarkRequest;
import in.instello.module.studentRemark.service.StudentRemarkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student-remarks")
@RequiredArgsConstructor
public class StudentRemarkController {

  private final StudentRemarkService studentRemarkService;

  // Get all remarks
  @GetMapping
  public List<StudentRemarkResponse> getAllRemarks() {
    return studentRemarkService.getAllRemarks();
  }

  // Get remark by ID
  @GetMapping("/{id}")
  public StudentRemarkResponse getRemarkById(@PathVariable String id) {

    return studentRemarkService.getRemarkById(id);
  }

  // Create remark
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public StudentRemarkResponse createRemark(
      @Valid @RequestBody CreateStudentRemarkRequest request) {

    return studentRemarkService.createRemark(request, "TEMP_USER_ID");
  }

  // Update remark
  @PutMapping("/{id}")
  public StudentRemarkResponse updateRemark(
      @PathVariable String id, @Valid @RequestBody UpdateStudentRemarkRequest request) {

    return studentRemarkService.updateRemark(id, request, "TEMP_USER_ID");
  }

  // Delete remark
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRemark(@PathVariable String id) {

    studentRemarkService.deleteRemark(id);
  }
}
