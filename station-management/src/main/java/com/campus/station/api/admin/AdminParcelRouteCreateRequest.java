package com.campus.station.api.admin;

import java.time.LocalDateTime;

public class AdminParcelRouteCreateRequest {

    private String trackingNumber;
    private String company;
    private String origin;
    private String destination;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

