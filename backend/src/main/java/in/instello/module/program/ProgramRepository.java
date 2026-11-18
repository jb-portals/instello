package in.instello.module.program;

import in.instello.module.institution.entity.Institution;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProgramRepository
    extends JpaRepository<Program, String>, JpaSpecificationExecutor<Program> {
  Optional<Program> findByIdOrAliasAndInstitution(String id, String alias, Institution institution);
}
