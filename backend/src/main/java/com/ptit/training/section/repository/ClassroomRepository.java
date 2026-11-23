package com.ptit.training.section.repository;

import com.ptit.training.section.entity.Classroom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    boolean existsByCodeIgnoreCase(String code);
    List<Classroom> findAllByOrderByCodeAsc();
}
