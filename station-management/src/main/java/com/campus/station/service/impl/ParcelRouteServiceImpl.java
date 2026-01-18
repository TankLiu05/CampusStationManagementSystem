package com.campus.station.service.impl;

import com.campus.station.model.ParcelRoute;
import com.campus.station.repository.ParcelRouteRepository;
import com.campus.station.service.ParcelRouteService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelRouteServiceImpl implements ParcelRouteService {

    private final ParcelRouteRepository repository;

    public ParcelRouteServiceImpl(ParcelRouteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ParcelRoute create(ParcelRoute route) {
        return repository.save(route);
    }

    @Override
    @Transactional
    public ParcelRoute updateEtaDelivered(Long id, LocalDateTime etaDelivered) {
        ParcelRoute existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("快递流转记录不存在"));
        existing.setEtaDelivered(etaDelivered);
        return repository.save(existing);
    }
}

