package com.ptit.training.grade.repository;

import com.ptit.training.grade.entity.StudentGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    Optional<StudentGrade> findByEnrollmentIdAndGradeComponentId(Long enrollmentId, Long gradeComponentId);

    @Query("""
            SELECT sg FROM StudentGrade sg
            JOIN FETCH sg.gradeComponent
            WHERE sg.enrollment.id = :enrollmentId
            """)
    List<StudentGrade> findByEnrollmentIdWithComponent(@Param("enrollmentId") Long enrollmentId);

    @Query("""
            SELECT sg FROM StudentGrade sg
            JOIN FETCH sg.enrollment e
            JOIN FETCH sg.gradeComponent gc
            WHERE e.section.id = :sectionId
            """)
    List<StudentGrade> findBySectionIdWithComponent(@Param("sectionId") Long sectionId);
}
