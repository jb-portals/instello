package in.instello.module.subjectCatalog;

import in.instello.module.institution.entity.Institution;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository
    extends JpaRepository<Subject, String>, JpaSpecificationExecutor<Subject> {
  Optional<Subject> findByIdOrAliasAndInstitution(String id, String alias, Institution institution);
}
