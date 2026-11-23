package com.ptit.training.organization.repository;

import com.ptit.training.organization.entity.Faculty;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    boolean existsByCodeIgnoreCase(String code);
    List<Faculty> findAllByOrderByCodeAsc();
    List<Faculty> findByStatusOrderByCodeAsc(Faculty.Status status);
}
