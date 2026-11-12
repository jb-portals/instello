package in.instello.module.studentRemark.entity;

import in.instello.annotations.GeneratedCuid;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student_remarks")
public class StudentRemark {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private RemarkCategory category;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private RemarkStatus status;

  @Column(nullable = false, length = 24, updatable = false)
  private String createdById;

  @Column(length = 24)
  private String updatedById;

  // One Remark can have multiple Attachments
  @OneToMany(mappedBy = "remark", fetch = FetchType.LAZY)
  @Builder.Default
  private List<RemarkAttachment> attachments = new ArrayList<>();

  // One Remark can have multiple History records
  @OneToMany(mappedBy = "remark", fetch = FetchType.LAZY)
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  @Builder.Default
  @Column(nullable = false)
  private boolean isDeleted = false;

  private Instant deletedAt;
}
