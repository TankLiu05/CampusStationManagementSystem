package com.campus.station.service.impl;

import com.campus.station.model.Location;
import com.campus.station.repository.LocationRepository;
import com.campus.station.service.LocationService;
import java.util.List;
import java.util.Optional;
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
        List<Location> existingLocations = repository.findByUserId(location.getUserId());

        if (existingLocations.isEmpty()) {
            // 如果用户还没有地址，设置为默认
            location.setIsDefault(true);
        } else {
            // 用户已有地址
            if (Boolean.TRUE.equals(location.getIsDefault())) {
                // 如果用户选择设置为默认，取消其他默认地址
                existingLocations.stream()
                        .filter(l -> Boolean.TRUE.equals(l.getIsDefault()))
                        .findFirst()
                        .ifPresent(existing -> {
                            existing.setIsDefault(false);
                            repository.save(existing);
                        });
            } else {
                // 如果用户未选择或选择不为默认，且已有地址，则设为非默认
                location.setIsDefault(false);
            }
        }
        return repository.save(location);
    }

    @Override
    public List<Location> listByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Optional<Location> getDefaultByUserId(Long userId) {
        return repository.findByUserIdAndIsDefault(userId, Boolean.TRUE);
    }

    @Override
    public Optional<Location> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Location update(Long id, Location update) {
        Location existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));

        if (update.getUsername() != null) existing.setUsername(update.getUsername());
        if (update.getPhone() != null) existing.setPhone(update.getPhone());
        if (update.getProvince() != null) existing.setProvince(update.getProvince());
        if (update.getCity() != null) existing.setCity(update.getCity());
        if (update.getStreet() != null) existing.setStreet(update.getStreet());
        if (update.getDetailAddress() != null) existing.setDetailAddress(update.getDetailAddress());

        if (Boolean.TRUE.equals(update.getIsDefault())) {
            setDefault(existing.getUserId(), id);
            existing.setIsDefault(true);
        }

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long locationId) {
        repository.findByUserIdAndIsDefault(userId, true)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    repository.save(existing);
                });
        
        repository.findById(locationId).ifPresent(location -> {
            location.setIsDefault(true);
            repository.save(location);
        });
    }
}
