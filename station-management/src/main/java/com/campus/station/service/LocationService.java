package com.campus.station.service;

import com.campus.station.model.Location;
import java.util.List;
import java.util.Optional;

public interface LocationService {

    Location create(Location location);

    List<Location> listByUserId(Long userId);

    Optional<Location> getDefaultByUserId(Long userId);

    Optional<Location> getById(Long id);

    Location update(Long id, Location location);

    void delete(Long id);

    void setDefault(Long userId, Long locationId);
}
