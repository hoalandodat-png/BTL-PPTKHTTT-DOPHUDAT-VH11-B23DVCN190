package com.ptit.training.organization.repository;

import com.ptit.training.organization.entity.Department;
import com.ptit.training.organization.entity.Faculty;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByFacultyId(Long facultyId);

    @EntityGraph(attributePaths = {"faculty"})
    List<Department> findAllByOrderByCodeAsc();

    @EntityGraph(attributePaths = {"faculty"})
    List<Department> findByFacultyIdAndStatusOrderByCodeAsc(Long facultyId, Faculty.Status status);

    @EntityGraph(attributePaths = {"faculty"})
    List<Department> findByFacultyIdOrderByCodeAsc(Long facultyId);
}
