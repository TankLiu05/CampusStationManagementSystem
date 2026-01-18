package com.campus.station.service;

import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ParcelService {
    Parcel create(Parcel parcel);
    Optional<Parcel> getById(Long id);
    Optional<Parcel> getByTrackingNumber(String trackingNumber);
    Page<Parcel> list(Pageable pageable);
    Page<Parcel> listByStatus(Integer status, Pageable pageable);
    Page<Parcel> listByStation(String stationCode, Pageable pageable);
    Page<Parcel> listByStationAndStatus(String stationCode, Integer status, Pageable pageable);
    Page<Parcel> listForScope(AdminRoleScope scope, Pageable pageable);
    Page<Parcel> listForScopeAndStatus(AdminRoleScope scope, Integer status, Pageable pageable);
    Parcel update(Long id, Parcel update);
    void delete(Long id);
    Parcel changeStatus(Long id, Integer status);
    Parcel markSigned(Long id);
    Page<Parcel> listByReceiver(Long receiverId, Pageable pageable);
    Optional<Parcel> getByTrackingNumberAndReceiverId(String trackingNumber, Long receiverId);
    Page<Parcel> listByReceiverAndIsSigned(Long receiverId, Integer isSigned, Pageable pageable);
    Optional<Parcel> findActiveByPickupCode(String pickupCode);
    Optional<Parcel> findActiveByLocation(String location);
    void deleteBatch(Iterable<Long> ids);
    void updateReceiverInfo(Long receiverId, String receiverName, String receiverPhone);
    boolean isParcelVisibleForStation(Parcel parcel, String stationCode);
    boolean isParcelVisibleForScope(AdminRoleScope scope, Parcel parcel);
}
