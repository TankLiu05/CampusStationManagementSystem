package com.campus.station.service.impl;

import com.campus.station.model.SysUser;
import com.campus.station.repository.SysUserRepository;
import com.campus.station.repository.SysAdminRepository;
import com.campus.station.service.SysUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository repository;
    private final SysAdminRepository adminRepository;

    public SysUserServiceImpl(SysUserRepository repository, SysAdminRepository adminRepository) {
        this.repository = repository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public SysUser create(SysUser user) {
        // 如果未提供用户名但提供了手机号，则默认用户名使用手机号
        if ((user.getUsername() == null || user.getUsername().isBlank()) && user.getPhone() != null && !user.getPhone().isBlank()) {
            user.setUsername(user.getPhone());
        }

        if (user.getUsername() != null) {
            boolean existsInUser = repository.existsByUsername(user.getUsername());
            boolean existsInAdmin = adminRepository.existsByUsername(user.getUsername());
            if (existsInUser || existsInAdmin) {
                throw new IllegalArgumentException("用户名已存在");
            }
        }
        if (user.getPhone() != null && repository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("手机号已存在");
        }
        return repository.save(user);
    }

    @Override
    public Optional<SysUser> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<SysUser> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Optional<SysUser> getByPhone(String phone) {
        return repository.findByPhone(phone);
    }

    @Override
    @Transactional
    public SysUser update(Long id, SysUser update) {
        SysUser existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 如果更新用户名且与他人冲突，拦截
        if (update.getUsername() != null && !update.getUsername().equals(existing.getUsername())) {
            boolean existsInUser = repository.existsByUsername(update.getUsername());
            boolean existsInAdmin = adminRepository.existsByUsername(update.getUsername());
            if (existsInUser || existsInAdmin) {
                throw new IllegalArgumentException("用户名已存在");
            }
            existing.setUsername(update.getUsername());
        }

        if (update.getPassword() != null) existing.setPassword(update.getPassword());
        if (update.getPhone() != null) existing.setPhone(update.getPhone());
        if (update.getEmail() != null) existing.setEmail(update.getEmail());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<SysUser> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public SysUser changeStatus(Long id, byte status) {
        SysUser existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        existing.setStatus(status);
        return repository.save(existing);
    }

    @Override
    @Transactional
    public SysUser updatePassword(Long id, String newPassword) {
        SysUser existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        existing.setPassword(newPassword);
        return repository.save(existing);
    }
}
