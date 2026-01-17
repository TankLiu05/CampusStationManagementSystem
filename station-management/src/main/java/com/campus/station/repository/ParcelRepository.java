package com.campus.station.repository;

import com.campus.station.model.Parcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    Optional<Parcel> findByTrackingNumber(String trackingNumber);
    Page<Parcel> findByReceiverId(Long receiverId, Pageable pageable);
    Optional<Parcel> findByTrackingNumberAndReceiverId(String trackingNumber, Long receiverId);
    Page<Parcel> findByStatus(Integer status, Pageable pageable);
    Page<Parcel> findByReceiverIdAndIsSigned(Long receiverId, Integer isSigned, Pageable pageable);
    Optional<Parcel> findByPickupCodeAndIsSigned(String pickupCode, Integer isSigned);
}
