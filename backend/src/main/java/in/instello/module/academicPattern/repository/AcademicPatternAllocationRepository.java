package in.instello.module.academicPattern.repository;

import in.instello.module.academicPattern.entity.AcademicPatternAllocation;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicPatternAllocationRepository
    extends JpaRepository<AcademicPatternAllocation, String> {

  boolean existsByAcademicPattern_Id(String academicPatternId);

  @Query(
      """
            select allocation.academicPattern.id
            from AcademicPatternAllocation allocation
            where allocation.academicPattern.id in :patternIds
            """)
  Set<String> findAllocatedPatternIds(@Param("patternIds") Collection<String> patternIds);
}
