package com.ptit.training.enrollment.repository;

import com.ptit.training.enrollment.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    boolean existsByStudentIdAndCourseIdAndSemesterIdAndStatus(
            Long studentId, Long courseId, Long semesterId, Enrollment.Status status);

    @Query("""
            SELECT COALESCE(SUM(e.course.credits), 0)
            FROM Enrollment e
            WHERE e.student.id = :studentId
              AND e.semester.id = :semesterId
              AND e.status = 'ENROLLED'
            """)
    int sumActiveCreditsByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    @Query("""
            SELECT COUNT(e) FROM Enrollment e
            WHERE e.student.id = :studentId
              AND e.course.id = :courseId
              AND e.semester.id <> :currentSemesterId
              AND e.status = 'ENROLLED'
            """)
    long countPreviousAttempts(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("currentSemesterId") Long currentSemesterId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.section s
            JOIN FETCH s.course
            JOIN FETCH s.semester
            LEFT JOIN FETCH s.lecturer
            WHERE e.student.id = :studentId
              AND e.semester.id = :semesterId
              AND e.status = 'ENROLLED'
            ORDER BY e.enrolledAt ASC
            """)
    List<Enrollment> findActiveByStudentAndSemesterWithDetails(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.student st
            JOIN FETCH st.academicClass
            JOIN FETCH st.user
            WHERE e.section.id = :sectionId
              AND e.status = 'ENROLLED'
            ORDER BY st.studentCode ASC
            """)
    List<Enrollment> findActiveRosterBySectionId(@Param("sectionId") Long sectionId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.section s
            JOIN FETCH s.course c
            JOIN FETCH s.semester sem
            WHERE e.student.id = :studentId
              AND e.status = 'ENROLLED'
            ORDER BY sem.startDate ASC, c.code ASC
            """)
    List<Enrollment> findAllEnrolledByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.student
            JOIN FETCH e.course
            JOIN FETCH e.semester
            JOIN FETCH e.section
            WHERE e.id = :id
            """)
    Optional<Enrollment> findWithDetailsById(@Param("id") Long id);
}
