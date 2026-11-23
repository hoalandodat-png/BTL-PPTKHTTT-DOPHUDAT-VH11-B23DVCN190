package com.ptit.training.grade.repository;

import com.ptit.training.grade.entity.GradingScale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradingScaleRepository extends JpaRepository<GradingScale, Long> {
    List<GradingScale> findAllByOrderByDisplayOrderAsc();
}
