package com.campus.station.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "parcel")
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, length = 50)
    private String trackingNumber;

    @Column(name = "company", length = 50)
    private String company;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "origin", length = 100)
    private String origin;

    @Column(name = "destination", length = 100)
    private String destination;

    @Column(name = "location", length = 100)
    private String location;

    @Transient
    private String currentStation;

    @Transient
    private String nextStation;

    @Transient
    private LocalDateTime etaNextStation;

    @Transient
    private LocalDateTime etaDelivered;

    @Transient
    private Integer isDelivered;

    @Column(name = "pickup_code", length = 6)
    private String pickupCode;

    @Column(name = "status")
    private Integer status = 0;

    @Column(name = "is_signed")
    private Integer isSigned = 0;

    /**
     * 0: 未退货, 1: 已退货
     */
    @Column(name = "is_returned")
    private Integer isReturned = 0;

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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(Integer isSigned) {
        this.isSigned = isSigned;
    }

    public Integer getIsReturned() {
        return isReturned != null ? isReturned : 0;
    }

    public void setIsReturned(Integer isReturned) {
        this.isReturned = isReturned;
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
