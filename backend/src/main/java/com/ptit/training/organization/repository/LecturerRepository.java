package com.ptit.training.organization.repository;

import com.ptit.training.organization.entity.Lecturer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    boolean existsByLecturerCodeIgnoreCase(String lecturerCode);
    Optional<Lecturer> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "department", "department.faculty"})
    @Query("""
            SELECT l FROM Lecturer l
            WHERE (:departmentId IS NULL OR l.department.id = :departmentId)
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(l.lecturerCode) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
            """)
    Page<Lecturer> search(
            @Param("departmentId") Long departmentId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
