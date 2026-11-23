package com.ptit.training.course.repository;

import com.ptit.training.course.entity.CoursePrerequisite;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {
    @EntityGraph(attributePaths = {"prerequisiteCourse", "course"})
    List<CoursePrerequisite> findByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);
}
