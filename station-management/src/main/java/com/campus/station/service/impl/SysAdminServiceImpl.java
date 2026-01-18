package com.campus.station.service.impl;

import com.campus.station.model.SysAdmin;
import com.campus.station.repository.SysAdminRepository;
import com.campus.station.service.SysAdminService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysAdminServiceImpl implements SysAdminService {

    private final SysAdminRepository repository;

    public SysAdminServiceImpl(SysAdminRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysAdmin> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysAdmin> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public SysAdmin create(SysAdmin admin) {
        return repository.save(admin);
    }

    @Override
    @Transactional
    public SysAdmin update(Long id, SysAdmin update) {
        SysAdmin existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("管理员不存在"));

        if (update.getUsername() != null) {
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
