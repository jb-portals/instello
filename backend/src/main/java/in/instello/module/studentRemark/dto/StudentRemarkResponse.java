package in.instello.module.studentRemark.dto;

import in.instello.module.studentRemark.entity.RemarkCategory;
import in.instello.module.studentRemark.entity.RemarkStatus;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRemarkResponse {

  private String id;

  private RemarkCategory category;

  private String title;

  private String description;

  private RemarkStatus status;

  private String createdById;

  private String updatedById;

  private Instant createdAt;

  private Instant updatedAt;

  private boolean isDeleted;

  private Instant deletedAt;
}
