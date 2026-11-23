package com.ptit.training.course.repository;

import com.ptit.training.course.entity.Course;
import com.ptit.training.organization.entity.Faculty;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"department", "department.faculty", "gradeComponents"})
    Optional<Course> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"department", "department.faculty"})
    @Query("""
            SELECT c FROM Course c
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.code) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
              AND (:departmentId IS NULL OR c.department.id = :departmentId)
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<Course> search(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("status") Faculty.Status status,
            Pageable pageable);
}
