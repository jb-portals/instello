package in.instello.module.studentRemark.controller;

import in.instello.module.studentRemark.entity.RemarkAttachment;
import in.instello.module.studentRemark.service.RemarkAttachmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/remark-attachments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RemarkAttachmentController {

  private final RemarkAttachmentService remarkAttachmentService;

  // Get all attachments
  @GetMapping
  public ResponseEntity<List<RemarkAttachment>> getAllAttachments() {

    return ResponseEntity.ok(remarkAttachmentService.getAllAttachments());
  }

  // Get attachment by ID
  @GetMapping("/{id}")
  public ResponseEntity<?> getAttachmentById(@PathVariable String id) {

    RemarkAttachment attachment = remarkAttachmentService.getAttachmentById(id);

    if (attachment != null) {
      return ResponseEntity.ok(attachment);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attachment not found");
    }
  }

  // Create attachment
  @PostMapping("/remark/{remarkId}")
  public ResponseEntity<?> createAttachment(
      @PathVariable String remarkId, @RequestBody RemarkAttachment attachment) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(remarkAttachmentService.createAttachment(remarkId, attachment));
  }

  // Get attachments by remark ID
  @GetMapping("/remark/{remarkId}")
  public ResponseEntity<List<RemarkAttachment>> getAttachmentsByRemarkId(
      @PathVariable String remarkId) {

    return ResponseEntity.ok(remarkAttachmentService.getAttachmentsByRemarkId(remarkId));
  }

  // Delete attachment
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteAttachment(@PathVariable String id) {

    RemarkAttachment attachment = remarkAttachmentService.getAttachmentById(id);

    if (attachment != null) {
      remarkAttachmentService.deleteAttachment(id);

      return ResponseEntity.ok("Attachment deleted successfully");
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attachment not found");
    }
  }
}
