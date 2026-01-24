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
        // 查询该快递单号的最新一条流转记录
        java.util.List<ParcelRoute> routes = repository.findByTrackingNumberOrderByCreateTimeAsc(route.getTrackingNumber());
        if (!routes.isEmpty()) {
            ParcelRoute lastRoute = routes.get(routes.size() - 1);
            // 如果最新记录的当前站点与新记录的当前站点相同，且最新记录没有下一站信息
            // 则认为是补充完善该站点的流转信息，直接更新原记录
            if (lastRoute.getCurrentStation().equals(route.getCurrentStation()) && lastRoute.getNextStation() == null) {
                lastRoute.setNextStation(route.getNextStation());
                lastRoute.setEtaNextStation(route.getEtaNextStation());
                lastRoute.setEtaDelivered(route.getEtaDelivered());
                return repository.save(lastRoute);
            }
        }
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

    @Override
    public java.util.List<ParcelRoute> listByTrackingNumber(String trackingNumber) {
        return repository.findByTrackingNumberOrderByCreateTimeAsc(trackingNumber);
    }
}

