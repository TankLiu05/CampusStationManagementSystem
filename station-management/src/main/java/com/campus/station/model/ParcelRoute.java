package com.campus.station.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "parcel_route")
public class ParcelRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, length = 50)
    private String trackingNumber;

    @Column(name = "current_station", nullable = false, length = 100)
    private String currentStation;

    @Column(name = "next_station", length = 100)
    private String nextStation;

    @Column(name = "eta_next_station")
    private LocalDateTime etaNextStation;

    @Column(name = "eta_delivered")
    private LocalDateTime etaDelivered;

    @Column(name = "is_delivered")
    private Integer isDelivered = 0;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getIsDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(Integer isDelivered) {
        this.isDelivered = isDelivered;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

