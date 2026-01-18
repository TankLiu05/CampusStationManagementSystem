package com.campus.station.repository;

import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRoleScopeRepository extends JpaRepository<AdminRoleScope, Long> {

    Optional<AdminRoleScope> findByAdminId(Long adminId);

    List<AdminRoleScope> findByParentAdminId(Long parentAdminId);

    List<AdminRoleScope> findByRole(AdminRole role);
}

