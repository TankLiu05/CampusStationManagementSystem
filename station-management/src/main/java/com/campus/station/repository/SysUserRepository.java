package com.campus.station.repository;

import com.campus.station.model.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<SysUser> findByPhone(String phone);
    boolean existsByPhone(String phone);
}