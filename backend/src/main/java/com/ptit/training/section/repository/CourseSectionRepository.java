package com.ptit.training.section.repository;

import com.ptit.training.section.entity.CourseSection;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    boolean existsByCourseIdAndSemesterIdAndGroupNumber(Long courseId, Long semesterId, Integer groupNumber);

    /**
     * Khóa bi quan dòng CourseSection (SELECT ... FOR UPDATE) chống Race Condition suất cuối (BR-25).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("""
            SELECT cs FROM CourseSection cs
            JOIN FETCH cs.course
            JOIN FETCH cs.semester
            WHERE cs.id = :id
            """)
    Optional<CourseSection> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "schedules", "schedules.classroom"})
    Optional<CourseSection> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer"})
    @Query("""
            SELECT cs FROM CourseSection cs
            WHERE (:semesterId IS NULL OR cs.semester.id = :semesterId)
              AND (:courseId IS NULL OR cs.course.id = :courseId)
              AND (:lecturerId IS NULL OR cs.lecturer.id = :lecturerId)
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(cs.sectionCode) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(cs.course.code) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                   OR LOWER(cs.course.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
            """)
    Page<CourseSection> search(
            @Param("semesterId") Long semesterId,
            @Param("courseId") Long courseId,
            @Param("lecturerId") Long lecturerId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "schedules", "schedules.classroom"})
    @Query("""
            SELECT cs FROM CourseSection cs
            WHERE cs.lecturer.id = :lecturerId
              AND cs.semester.id = :semesterId
              AND cs.status <> 'CANCELLED'
            """)
    List<CourseSection> findActiveByLecturerAndSemester(
            @Param("lecturerId") Long lecturerId,
            @Param("semesterId") Long semesterId);
}
