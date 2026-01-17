package com.campus.station.service;

import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ParcelStorageService {
    ParcelStorage createForParcel(Parcel parcel, String pickupCode, String location);
    Optional<ParcelStorage> getByParcelId(Long parcelId);
    Optional<ParcelStorage> getById(Long id);
    Page<ParcelStorage> list(Pageable pageable);
    ParcelStorage update(Long id, ParcelStorage update);
    void delete(Long id);
    void markSignedByParcelId(Long parcelId);
    Optional<ParcelStorage> findActiveByPickupCode(String pickupCode);
}
