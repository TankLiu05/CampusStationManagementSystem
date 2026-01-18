package com.campus.station.service.impl;

import com.campus.station.model.AdminRoleScope;
import com.campus.station.repository.AdminRoleScopeRepository;
import com.campus.station.service.AdminRoleScopeService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRoleScopeServiceImpl implements AdminRoleScopeService {

    private final AdminRoleScopeRepository repository;

    public AdminRoleScopeServiceImpl(AdminRoleScopeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AdminRoleScope> getByAdminId(Long adminId) {
        return repository.findByAdminId(adminId);
    }

    @Override
    public List<AdminRoleScope> getByParentAdminId(Long parentAdminId) {
        return repository.findByParentAdminId(parentAdminId);
    }

    @Override
    @Transactional
    public AdminRoleScope create(AdminRoleScope scope) {
        return repository.save(scope);
    }

    @Override
    @Transactional
    public AdminRoleScope update(Long id, AdminRoleScope update) {
        AdminRoleScope existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("管理员角色范围不存在"));

        if (update.getRole() != null) {
            existing.setRole(update.getRole());
        }
        if (update.getProvince() != null) {
            existing.setProvince(update.getProvince());
        }
        if (update.getCity() != null) {
            existing.setCity(update.getCity());
        }
        if (update.getStationId() != null) {
            existing.setStationId(update.getStationId());
        }

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}

