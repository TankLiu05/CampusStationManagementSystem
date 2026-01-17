package com.campus.station.repository;

import com.campus.station.model.ParcelStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParcelStorageRepository extends JpaRepository<ParcelStorage, Long> {
    Optional<ParcelStorage> findByParcelId(Long parcelId);
    Optional<ParcelStorage> findByPickupCodeAndSignedTimeIsNull(String pickupCode);
}
