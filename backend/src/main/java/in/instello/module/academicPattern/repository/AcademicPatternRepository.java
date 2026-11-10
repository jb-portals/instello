package in.instello.module.academicPattern.repository;

import in.instello.module.academicPattern.entity.AcademicPattern;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicPatternRepository extends JpaRepository<AcademicPattern, String> {

  List<AcademicPattern> findByOrganization_IdOrderByNameAsc(String organizationId);

  Optional<AcademicPattern> findByIdAndOrganization_Id(String id, String organizationId);
}
