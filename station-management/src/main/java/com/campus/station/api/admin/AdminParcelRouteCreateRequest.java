package com.campus.station.api.admin;

import java.time.LocalDateTime;

public class AdminParcelRouteCreateRequest {

    private String trackingNumber;
    private String currentStation;
    private String nextStation;
    private LocalDateTime etaNextStation;
    private LocalDateTime etaDelivered;

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(String currentStation) {
        this.currentStation = currentStation;
    }

    public String getNextStation() {
        return nextStation;
    }

    public void setNextStation(String nextStation) {
        this.nextStation = nextStation;
    }

    public LocalDateTime getEtaNextStation() {
        return etaNextStation;
    }

    public void setEtaNextStation(LocalDateTime etaNextStation) {
        this.etaNextStation = etaNextStation;
    }

    public LocalDateTime getEtaDelivered() {
        return etaDelivered;
    }

    public void setEtaDelivered(LocalDateTime etaDelivered) {
        this.etaDelivered = etaDelivered;
    }
}

