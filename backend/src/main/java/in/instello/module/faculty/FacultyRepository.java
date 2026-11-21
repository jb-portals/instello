package in.instello.module.faculty;

import in.instello.module.institution.entity.Institution;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface FacultyRepository
    extends JpaRepository<Faculty, String>, JpaSpecificationExecutor<Faculty> {
  Optional<Faculty> findByIdOrStaffIdAndInstitution(
      String id, String staffId, Institution institution);

  Optional<Faculty> findByIdOrStaffIdInInstitution(
      @Param("identifier") String identifier, @Param("institution") Institution institution);

  Optional<Faculty> findByEmailAndInstitution(String email, Institution institution);

  Optional<Faculty> findByPrimaryPhoneNumberAndInstitution(
      String primaryPhoneNumber, Institution institution);
}
