package in.instello.module.programSubject.repository;

import in.instello.module.institution.entity.Institution;
import in.instello.module.programSubject.entity.SubjectType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubjectTypeRepository
    extends JpaRepository<SubjectType, String>, JpaSpecificationExecutor<SubjectType> {
  @Query(
      """
        SELECT st FROM SubjectType st
        WHERE (st.id = :id OR st.name = :name)
        AND st.institution = :institution
    """)
  Optional<SubjectType> findByIdOrNameInInstitution(
      @Param("id") String id,
      @Param("name") String name,
      @Param("institution") Institution institution);
}
