package com.campus.station.api.admin;

import java.time.LocalDateTime;

public class AdminParcelRouteUpdateEtaRequest {

    private LocalDateTime etaDelivered;

    public LocalDateTime getEtaDelivered() {
        return etaDelivered;
    }

    public void setEtaDelivered(LocalDateTime etaDelivered) {
        this.etaDelivered = etaDelivered;
    }
}

