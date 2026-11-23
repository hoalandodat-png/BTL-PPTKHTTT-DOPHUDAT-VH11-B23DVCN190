package com.ptit.training.section.repository;

import com.ptit.training.section.entity.AcademicYear;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    boolean existsByCodeIgnoreCase(String code);
    List<AcademicYear> findAllByOrderByStartYearDesc();
}
