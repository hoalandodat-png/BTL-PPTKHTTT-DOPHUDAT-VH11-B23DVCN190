package com.ptit.training.section.repository;

import com.ptit.training.section.entity.Semester;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    boolean existsByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"academicYear"})
    List<Semester> findAllByOrderByStartDateDesc();
}
