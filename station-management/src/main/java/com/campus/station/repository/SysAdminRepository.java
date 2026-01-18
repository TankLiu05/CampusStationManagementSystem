package com.campus.station.repository;

import com.campus.station.model.SysAdmin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysAdminRepository extends JpaRepository<SysAdmin, Long> {

    Optional<SysAdmin> findByUserId(Long userId);
}

