package com.ptit.training.auth.repository;

import com.ptit.training.auth.entity.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findAllByOrderByModuleAscIdAsc();
}
