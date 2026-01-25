package com.campus.station.repository;

import com.campus.station.model.StationStorage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StationStorageRepository extends JpaRepository<StationStorage, Long>, JpaSpecificationExecutor<StationStorage> {
    
    Optional<StationStorage> findByTrackingNumber(String trackingNumber);
    
    Optional<StationStorage> findByPickupCode(String pickupCode);
    
    // 检查位置是否被占用 (未签收的包裹)
    boolean existsByAreaAndShelfAndPositionAndIsSigned(String area, String shelf, String position, Integer isSigned);

    java.util.List<StationStorage> findByStationNameIsNull();
}
