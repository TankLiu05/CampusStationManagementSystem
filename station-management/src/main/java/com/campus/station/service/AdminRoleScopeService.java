package com.campus.station.service;

import com.campus.station.model.AdminRoleScope;
import java.util.List;
import java.util.Optional;

public interface AdminRoleScopeService {

    Optional<AdminRoleScope> getByAdminId(Long adminId);

    List<AdminRoleScope> getByParentAdminId(Long parentAdminId);

    List<AdminRoleScope> listAll();

    AdminRoleScope create(AdminRoleScope scope);

    AdminRoleScope update(Long id, AdminRoleScope update);

    void delete(Long id);
}
