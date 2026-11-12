package in.instello.module.studentRemark.dto;

import in.instello.module.studentRemark.entity.RemarkCategory;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRemarkRequest {

  private RemarkCategory category;

  @Size(max = 200)
  private String title;

  private String description;
}
