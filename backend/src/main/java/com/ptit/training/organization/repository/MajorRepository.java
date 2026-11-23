package com.ptit.training.organization.repository;

import com.ptit.training.organization.entity.Major;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByFacultyId(Long facultyId);
    Optional<Major> findByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"faculty"})
    List<Major> findAllByOrderByCodeAsc();

    @EntityGraph(attributePaths = {"faculty"})
    List<Major> findByFacultyIdOrderByCodeAsc(Long facultyId);
}
