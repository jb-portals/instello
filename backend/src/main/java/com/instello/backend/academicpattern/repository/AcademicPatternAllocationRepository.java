package com.instello.backend.academicpattern.repository;

import com.instello.backend.academicpattern.entity.AcademicPatternAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface AcademicPatternAllocationRepository extends JpaRepository<AcademicPatternAllocation, UUID> {

    boolean existsByAcademicPattern_Id(UUID academicPatternId);

    @Query("""
            select allocation.academicPattern.id
            from AcademicPatternAllocation allocation
            where allocation.academicPattern.id in :patternIds
            """)
    Set<UUID> findAllocatedPatternIds(@Param("patternIds") Collection<UUID> patternIds);
}
