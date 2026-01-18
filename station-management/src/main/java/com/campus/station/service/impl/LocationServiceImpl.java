package com.campus.station.service.impl;

import com.campus.station.model.Location;
import com.campus.station.repository.LocationRepository;
import com.campus.station.service.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository repository;

    public LocationServiceImpl(LocationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Location create(Location location) {
        return repository.save(location);
    }
}

