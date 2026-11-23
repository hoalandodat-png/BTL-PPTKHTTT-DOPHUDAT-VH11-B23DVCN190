package com.ptit.training.organization.repository;

import com.ptit.training.organization.entity.AcademicClass;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<AcademicClass> findByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"major", "major.faculty"})
    @Query("""
            SELECT ac FROM AcademicClass ac
            WHERE (:majorId IS NULL OR ac.major.id = :majorId)
              AND (:cohortYear IS NULL OR ac.cohortYear = :cohortYear)
            ORDER BY ac.cohortYear DESC, ac.code ASC
            """)
    List<AcademicClass> search(@Param("majorId") Long majorId, @Param("cohortYear") Integer cohortYear);
}
