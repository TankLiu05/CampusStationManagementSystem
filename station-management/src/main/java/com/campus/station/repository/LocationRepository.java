package com.campus.station.repository;

import com.campus.station.model.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByUserId(Long userId);

    Optional<Location> findByUserIdAndIsDefault(Long userId, Boolean isDefault);
}
