package in.instello.module.studentRemark.service;

import in.instello.module.studentRemark.entity.RemarkAttachment;
import in.instello.module.studentRemark.entity.StudentRemark;
import in.instello.module.studentRemark.repository.RemarkAttachmentRepository;
import in.instello.module.studentRemark.repository.StudentRemarkRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemarkAttachmentService {

  private final RemarkAttachmentRepository repository;
  private final StudentRemarkRepository studentRemarkRepository;

  // Get all attachments
  public List<RemarkAttachment> getAllAttachments() {
    return repository.findAll();
  }

  // Get attachment by ID
  public RemarkAttachment getAttachmentById(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + id));
  }

  // Create attachment
  public RemarkAttachment createAttachment(String remarkId, RemarkAttachment attachment) {

    StudentRemark remark =
        studentRemarkRepository
            .findById(remarkId)
            .filter(r -> !r.isDeleted())
            .orElseThrow(() -> new EntityNotFoundException("Remark not found: " + remarkId));

    attachment.setRemark(remark);

    return repository.save(attachment);
  }

  // Get attachments by remark ID
  public List<RemarkAttachment> getAttachmentsByRemarkId(String remarkId) {

    return repository.findByRemark_Id(remarkId);
  }

  // Delete attachment
  public void deleteAttachment(String id) {
    RemarkAttachment attachment = getAttachmentById(id);
    repository.delete(attachment);
  }
}
