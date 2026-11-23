package com.ptit.training.section.repository;

import com.ptit.training.section.entity.ExamSchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    @Query("""
            SELECT es FROM ExamSchedule es
            JOIN FETCH es.section s
            JOIN FETCH s.course
            JOIN FETCH s.semester
            JOIN FETCH es.classroom
            WHERE s.semester.id = :semesterId
            ORDER BY es.examDate ASC, es.startTime ASC
            """)
    List<ExamSchedule> findBySemesterIdWithDetails(@Param("semesterId") Long semesterId);

    @Query("""
            SELECT es FROM ExamSchedule es
            JOIN FETCH es.section s
            JOIN FETCH s.course
            JOIN FETCH s.semester
            JOIN FETCH es.classroom
            WHERE s.id IN :sectionIds
            ORDER BY es.examDate ASC, es.startTime ASC
            """)
    List<ExamSchedule> findBySectionIdsWithDetails(@Param("sectionIds") List<Long> sectionIds);

    @Query("""
            SELECT es FROM ExamSchedule es
            JOIN FETCH es.section s
            WHERE es.classroom.id = :classroomId
              AND es.examDate = :examDate
            """)
    List<ExamSchedule> findByClassroomAndDate(
            @Param("classroomId") Long classroomId,
            @Param("examDate") LocalDate examDate);
}
