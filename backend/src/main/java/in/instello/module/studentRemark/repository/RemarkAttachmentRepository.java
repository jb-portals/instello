package in.instello.module.studentRemark.repository;

import in.instello.module.studentRemark.entity.RemarkAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemarkAttachmentRepository extends JpaRepository<RemarkAttachment, String> {

  List<RemarkAttachment> findByRemark_Id(String remarkId);
}
