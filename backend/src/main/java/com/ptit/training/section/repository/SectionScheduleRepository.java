package com.ptit.training.section.repository;

import com.ptit.training.section.entity.SectionSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SectionScheduleRepository extends JpaRepository<SectionSchedule, Long> {

    @Query("""
            SELECT ss FROM SectionSchedule ss
            JOIN FETCH ss.section s
            JOIN FETCH s.course
            JOIN FETCH ss.classroom
            WHERE s.semester.id = :semesterId
              AND ss.classroom.id = :classroomId
              AND s.status <> 'CANCELLED'
              AND (:excludeSectionId IS NULL OR s.id <> :excludeSectionId)
            """)
    List<SectionSchedule> findBySemesterAndClassroomExcludingSection(
            @Param("semesterId") Long semesterId,
            @Param("classroomId") Long classroomId,
            @Param("excludeSectionId") Long excludeSectionId);

    @Query("""
            SELECT ss FROM SectionSchedule ss
            JOIN FETCH ss.section s
            JOIN FETCH s.course
            JOIN FETCH ss.classroom
            WHERE s.semester.id = :semesterId
              AND s.lecturer.id = :lecturerId
              AND s.status <> 'CANCELLED'
              AND (:excludeSectionId IS NULL OR s.id <> :excludeSectionId)
            """)
    List<SectionSchedule> findBySemesterAndLecturerExcludingSection(
            @Param("semesterId") Long semesterId,
            @Param("lecturerId") Long lecturerId,
            @Param("excludeSectionId") Long excludeSectionId);

    @Query("""
            SELECT ss FROM SectionSchedule ss
            JOIN FETCH ss.classroom
            WHERE ss.section.id = :sectionId
            ORDER BY ss.dayOfWeek ASC, ss.startPeriod ASC
            """)
    List<SectionSchedule> findBySectionIdWithClassroom(@Param("sectionId") Long sectionId);
}
