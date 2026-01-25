package com.campus.station.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.campus.station.model.Parcel;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    Optional<Parcel> findByTrackingNumber(String trackingNumber);
    Page<Parcel> findByReceiverId(Long receiverId, Pageable pageable);
    Optional<Parcel> findByTrackingNumberAndReceiverId(String trackingNumber, Long receiverId);
    Page<Parcel> findByStatus(Integer status, Pageable pageable);
    Page<Parcel> findByReceiverIdAndIsSigned(Long receiverId, Integer isSigned, Pageable pageable);
    Optional<Parcel> findByPickupCodeAndIsSigned(String pickupCode, Integer isSigned);
    Optional<Parcel> findByLocationAndIsSigned(String location, Integer isSigned);
    Optional<Parcel> findByLocationAndCurrentStationAndIsSigned(String location, String currentStation, Integer isSigned);
    List<Parcel> findByReceiverId(Long receiverId);
    
    // 通过手机号查询包裹
    Page<Parcel> findByReceiverPhone(String receiverPhone, Pageable pageable);
    Optional<Parcel> findByTrackingNumberAndReceiverPhone(String trackingNumber, String receiverPhone);
    
    // 同时通过ID或手机号查询
    Page<Parcel> findByReceiverIdOrReceiverPhone(Long receiverId, String receiverPhone, Pageable pageable);
}
