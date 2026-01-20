package com.campus.station.repository;

import com.campus.station.model.SysAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysAdminRepository extends JpaRepository<SysAdmin, Long> {

    java.util.Optional<SysAdmin> findByUsername(String username);

    boolean existsByUsername(String username);
}
