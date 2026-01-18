package com.campus.station.repository;

import com.campus.station.model.ParcelRoute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRouteRepository extends JpaRepository<ParcelRoute, Long> {
    List<ParcelRoute> findByTrackingNumberOrderByCreateTimeAsc(String trackingNumber);
}

