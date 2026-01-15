package com.campus.station.service;

import com.campus.station.model.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SysUserService {
    SysUser create(SysUser user);
    Optional<SysUser> getById(Long id);
    Optional<SysUser> getByUsername(String username);
    Optional<SysUser> getByPhone(String phone);
    SysUser update(Long id, SysUser update);
    void delete(Long id);
    Page<SysUser> list(Pageable pageable);
    SysUser changeStatus(Long id, byte status);
}