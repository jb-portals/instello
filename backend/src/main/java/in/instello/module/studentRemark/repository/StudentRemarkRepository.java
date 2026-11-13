package in.instello.module.studentRemark.repository;

import in.instello.module.studentRemark.entity.StudentRemark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRemarkRepository extends JpaRepository<StudentRemark, String> {

  @Query("SELECT r FROM StudentRemark r WHERE r.isDeleted = false")
  List<StudentRemark> findAllActive();

  @Query("SELECT r FROM StudentRemark r WHERE r.id = :id AND r.isDeleted = false")
  Optional<StudentRemark> findActiveById(@Param("id") String id);
}
