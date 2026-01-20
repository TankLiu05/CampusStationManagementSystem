package com.campus.station.service;

import com.campus.station.model.SysAdmin;
import java.util.Optional;

public interface SysAdminService {

    Optional<SysAdmin> getById(Long id);

    Optional<SysAdmin> getByUsername(String username);

    SysAdmin create(SysAdmin admin);

    SysAdmin update(Long id, SysAdmin update);

    void delete(Long id);
}
