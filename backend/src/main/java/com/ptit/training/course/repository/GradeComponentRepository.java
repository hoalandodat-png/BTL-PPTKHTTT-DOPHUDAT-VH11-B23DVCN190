package com.ptit.training.course.repository;

import com.ptit.training.course.entity.GradeComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeComponentRepository extends JpaRepository<GradeComponent, Long> {
    List<GradeComponent> findByCourseIdOrderByDisplayOrderAsc(Long courseId);
}
