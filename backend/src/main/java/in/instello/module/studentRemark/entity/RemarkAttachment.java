package in.instello.module.studentRemark.entity;

import in.instello.annotations.GeneratedCuid;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "remark_attachments")
public class RemarkAttachment {

  @Id
  @GeneratedCuid
  @Column(length = 24, nullable = false, updatable = false)
  private String id;

  // Many Attachments belong to One Remark
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "remark_id", nullable = false)
  private StudentRemark remark;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 500)
  private String fileUrl;

  @Column(length = 100)
  private String fileType;

  private Long fileSize;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
