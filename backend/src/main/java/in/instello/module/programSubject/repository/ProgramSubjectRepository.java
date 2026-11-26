package in.instello.module.programSubject.repository;

import in.instello.module.institution.entity.Institution;
import in.instello.module.programSubject.entity.ProgramSubject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProgramSubjectRepository
    extends JpaRepository<ProgramSubject, String>, JpaSpecificationExecutor<ProgramSubject> {
  Optional<ProgramSubject> findByIdAndInstitution(String id, Institution institution);
}
