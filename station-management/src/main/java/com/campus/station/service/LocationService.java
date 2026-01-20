package com.campus.station.service;

import com.campus.station.model.Location;
import java.util.List;

public interface LocationService {

    Location create(Location location);

    List<Location> listByUserId(Long userId);
}
