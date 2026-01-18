package com.campus.station.service;

import com.campus.station.model.ParcelRoute;
import java.time.LocalDateTime;

public interface ParcelRouteService {

    ParcelRoute create(ParcelRoute route);

    ParcelRoute updateEtaDelivered(Long id, LocalDateTime etaDelivered);
}

