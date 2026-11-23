package com.ptit.training.student.repository;

import com.ptit.training.student.entity.StudentStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    @EntityGraph(attributePaths = {"changedBy"})
    List<StudentStatusHistory> findByStudentIdOrderByChangedAtDesc(Long studentId);
}
