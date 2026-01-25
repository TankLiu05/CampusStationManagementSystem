package com.campus.station.service;

import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;

public interface ParcelService {
    Parcel create(Parcel parcel);
    Parcel create(Parcel parcel, String initialStation);
    Optional<Parcel> getById(Long id);
    Optional<Parcel> getByTrackingNumber(String trackingNumber);
    Page<Parcel> list(Pageable pageable);
    Page<Parcel> listByStatus(Integer status, Pageable pageable);
    Page<Parcel> listByStation(String stationCode, Pageable pageable);
    Page<Parcel> listByStationAndStatus(String stationCode, Integer status, Pageable pageable);
    Page<Parcel> listForScope(AdminRoleScope scope, Pageable pageable);
    Page<Parcel> listForScopeAndStatus(AdminRoleScope scope, Integer status, Pageable pageable);

    Parcel update(Long id, Parcel parcel);
    
    void updateLogisticsInfo(Long id, String currentStation, String nextStation, java.time.LocalDateTime etaNextStation, java.time.LocalDateTime etaDelivered, Integer status);

    void delete(Long id);
    Parcel changeStatus(Long id, Integer status);
    Parcel markSigned(Long id);
    Page<Parcel> listByReceiver(Long receiverId, Pageable pageable);
    Optional<Parcel> getByTrackingNumberAndReceiverId(String trackingNumber, Long receiverId);
    Page<Parcel> listByReceiverAndIsSigned(Long receiverId, Integer isSigned, Pageable pageable);
    Optional<Parcel> findActiveByPickupCode(String pickupCode);
    Optional<Parcel> findActiveByLocation(String location);
    Optional<Parcel> findActiveByLocationAndStation(String location, String station);
    void deleteBatch(Iterable<Long> ids);
    void updateReceiverInfo(Long receiverId, String receiverName, String receiverPhone);
    boolean isParcelVisibleForStation(Parcel parcel, String stationCode);
    boolean isParcelVisibleForScope(AdminRoleScope scope, Parcel parcel);
    
    // 通过手机号查询包裹
    Page<Parcel> listByReceiverPhone(String receiverPhone, Pageable pageable);
    Optional<Parcel> getByTrackingNumberAndReceiverPhone(String trackingNumber, String receiverPhone);
    
    // 同时通过ID或手机号查询
    Page<Parcel> listByReceiverIdOrReceiverPhone(Long receiverId, String receiverPhone, Pageable pageable);
}
