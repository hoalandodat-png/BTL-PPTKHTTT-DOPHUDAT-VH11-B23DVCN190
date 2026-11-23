package com.ptit.training.course.repository;

import com.ptit.training.course.entity.Curriculum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByMajorIdAndCohortYear(Long majorId, Integer cohortYear);

    @EntityGraph(attributePaths = {"major", "curriculumCourses", "curriculumCourses.course"})
    Optional<Curriculum> findWithCoursesById(Long id);

    @EntityGraph(attributePaths = {"major", "curriculumCourses", "curriculumCourses.course"})
    Optional<Curriculum> findByMajorIdAndCohortYear(Long majorId, Integer cohortYear);

    @EntityGraph(attributePaths = {"major"})
    @Query("""
            SELECT c FROM Curriculum c
            WHERE (:majorId IS NULL OR c.major.id = :majorId)
              AND (:cohortYear IS NULL OR c.cohortYear = :cohortYear)
            ORDER BY c.cohortYear DESC, c.code ASC
            """)
    List<Curriculum> search(@Param("majorId") Long majorId, @Param("cohortYear") Integer cohortYear);
}
