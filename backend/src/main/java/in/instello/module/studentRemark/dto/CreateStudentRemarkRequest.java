package in.instello.module.studentRemark.dto;

import in.instello.module.studentRemark.entity.RemarkCategory;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentRemarkRequest {

  @NotNull private RemarkCategory category;

  @NotBlank
  @Size(max = 200)
  private String title;

  @NotBlank private String description;
}
