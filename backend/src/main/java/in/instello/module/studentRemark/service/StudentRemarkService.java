package in.instello.module.studentRemark.service;

import in.instello.module.studentRemark.dto.CreateStudentRemarkRequest;
import in.instello.module.studentRemark.dto.StudentRemarkResponse;
import in.instello.module.studentRemark.dto.UpdateStudentRemarkRequest;
import in.instello.module.studentRemark.entity.RemarkStatus;
import in.instello.module.studentRemark.entity.StudentRemark;
import in.instello.module.studentRemark.repository.StudentRemarkRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentRemarkService {

  private final StudentRemarkRepository repository;

  // 1. Get all active remarks
  public List<StudentRemarkResponse> getAllRemarks() {

    List<StudentRemark> remarks = repository.findAllActive();

    List<StudentRemarkResponse> responseList = new ArrayList<>();

    for (StudentRemark remark : remarks) {

      StudentRemarkResponse response = convertToResponse(remark);

      responseList.add(response);
    }

    return responseList;
  }

  // 2. Get remark by ID
  public StudentRemarkResponse getRemarkById(String id) {

    StudentRemark remark = getActiveRemark(id);

    StudentRemarkResponse response = convertToResponse(remark);

    return response;
  }

  // 3. Create a new remark
  @Transactional
  public StudentRemarkResponse createRemark(
      CreateStudentRemarkRequest request, String createdById) {

    StudentRemark remark = new StudentRemark();

    remark.setCategory(request.getCategory());
    remark.setTitle(request.getTitle());
    remark.setDescription(request.getDescription());
    remark.setStatus(RemarkStatus.PENDING);
    remark.setCreatedById(createdById);
    remark.setDeleted(false);

    StudentRemark savedRemark = repository.save(remark);

    StudentRemarkResponse response = convertToResponse(savedRemark);

    return response;
  }

  // 4. Update an existing remark
  @Transactional
  public StudentRemarkResponse updateRemark(
      String id, UpdateStudentRemarkRequest request, String updatedById) {

    StudentRemark remark = getActiveRemark(id);

    if (request.getCategory() != null) {
      remark.setCategory(request.getCategory());
    }

    if (request.getTitle() != null) {
      remark.setTitle(request.getTitle());
    }

    if (request.getDescription() != null) {
      remark.setDescription(request.getDescription());
    }

    remark.setUpdatedById(updatedById);

    StudentRemark updatedRemark = repository.save(remark);

    StudentRemarkResponse response = convertToResponse(updatedRemark);

    return response;
  }

  // 5. Delete remark
  @Transactional
  public void deleteRemark(String id) {

    StudentRemark remark = getActiveRemark(id);

    remark.setDeleted(true);
    remark.setDeletedAt(Instant.now());

    repository.save(remark);
  }

  // 6. Find active remark by ID
  private StudentRemark getActiveRemark(String id) {

    StudentRemark remark =
        repository
            .findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Student remark not found: " + id));

    return remark;
  }

  // 7. Convert Entity into Response DTO
  private StudentRemarkResponse convertToResponse(StudentRemark remark) {

    StudentRemarkResponse response = new StudentRemarkResponse();

    response.setId(remark.getId());
    response.setCategory(remark.getCategory());
    response.setTitle(remark.getTitle());
    response.setDescription(remark.getDescription());
    response.setStatus(remark.getStatus());
    response.setCreatedById(remark.getCreatedById());
    response.setUpdatedById(remark.getUpdatedById());
    response.setCreatedAt(remark.getCreatedAt());
    response.setUpdatedAt(remark.getUpdatedAt());
    response.setDeleted(remark.isDeleted());
    response.setDeletedAt(remark.getDeletedAt());

    return response;
  }
}
