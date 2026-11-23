package com.ptit.training.student.repository;

import com.ptit.training.student.entity.Student;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCodeIgnoreCase(String studentCode);
    boolean existsByNationalIdIgnoreCase(String nationalId);

    @EntityGraph(attributePaths = {"user", "major", "major.faculty", "academicClass"})
    Optional<Student> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "major", "major.faculty", "academicClass"})
    Optional<Student> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    Optional<Student> findByUserIdForUpdate(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT s FROM Student s WHERE s.id = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "major", "major.faculty", "academicClass"})
    @Query("""
            SELECT s FROM Student s
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
              AND (:majorId IS NULL OR s.major.id = :majorId)
              AND (:academicClassId IS NULL OR s.academicClass.id = :academicClassId)
              AND (:cohortYear IS NULL OR s.cohortYear = :cohortYear)
              AND (:academicStatus IS NULL OR s.academicStatus = :academicStatus)
            """)
    Page<Student> search(
            @Param("keyword") String keyword,
            @Param("majorId") Long majorId,
            @Param("academicClassId") Long academicClassId,
            @Param("cohortYear") Integer cohortYear,
            @Param("academicStatus") Student.AcademicStatus academicStatus,
            Pageable pageable);
}
