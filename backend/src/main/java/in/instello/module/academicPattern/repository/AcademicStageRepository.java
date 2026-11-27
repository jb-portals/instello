package in.instello.module.academicPattern.repository;

import in.instello.module.academicPattern.entity.AcademicStage;
import in.instello.module.institution.entity.Institution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicStageRepository extends JpaRepository<AcademicStage, String> {

  List<AcademicStage> findByAcademicPattern_IdOrderBySequenceNumberAsc(String academicPatternId);

  @Query(
      """
      select stage from Institution institution
      join institution.academicPattern pattern
      join pattern.academicStages stage
      where institution = :institution
        and (stage.id = :idOrAlias or stage.alias = :idOrAlias)
      """)
  Optional<AcademicStage> findByIdOrAliasInInstitution(
      @Param("institution") Institution institution, @Param("idOrAlias") String idOrAlias);

  @Modifying
  @Query("delete from AcademicStage stage where stage.academicPattern.id = :patternId")
  void deleteAllForPattern(@Param("patternId") String patternId);
}
