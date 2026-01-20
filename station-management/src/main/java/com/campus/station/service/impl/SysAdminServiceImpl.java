package com.campus.station.service.impl;

import com.campus.station.model.SysAdmin;
import com.campus.station.repository.SysAdminRepository;
import com.campus.station.repository.SysUserRepository;
import com.campus.station.service.SysAdminService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysAdminServiceImpl implements SysAdminService {

    private final SysAdminRepository repository;
    private final SysUserRepository userRepository;

    public SysAdminServiceImpl(SysAdminRepository repository, SysUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysAdmin> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysAdmin> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    @Transactional
    public SysAdmin create(SysAdmin admin) {
        if (admin.getUsername() != null) {
            boolean existsInAdmin = repository.existsByUsername(admin.getUsername());
            boolean existsInUser = userRepository.existsByUsername(admin.getUsername());
            if (existsInAdmin || existsInUser) {
                throw new IllegalArgumentException("用户名已存在");
            }
        }
        return repository.save(admin);
    }

    @Override
    @Transactional
    public SysAdmin update(Long id, SysAdmin update) {
        SysAdmin existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("管理员不存在"));

        if (update.getUsername() != null && !update.getUsername().equals(existing.getUsername())) {
            boolean existsInAdmin = repository.existsByUsername(update.getUsername());
            boolean existsInUser = userRepository.existsByUsername(update.getUsername());
            if (existsInAdmin || existsInUser) {
                throw new IllegalArgumentException("用户名已存在");
            }
            existing.setUsername(update.getUsername());
        }
        if (update.getPassword() != null) {
            existing.setPassword(update.getPassword());
        }
        if (update.getPhone() != null) {
            existing.setPhone(update.getPhone());
        }
        if (update.getEmail() != null) {
            existing.setEmail(update.getEmail());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
