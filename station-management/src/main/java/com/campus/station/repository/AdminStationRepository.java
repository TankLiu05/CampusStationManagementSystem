package com.campus.station.repository;

import com.campus.station.model.AdminStation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminStationRepository extends JpaRepository<AdminStation, Long> {

    boolean existsByProvinceAndCityAndStation(String province, String city, String station);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM AdminStation s, AdminRoleScope r WHERE r.role = :role AND r.station = s.station AND r.city = s.city AND r.province = s.province")
    java.util.List<AdminStation> findActiveStations(@org.springframework.data.repository.query.Param("role") com.campus.station.model.AdminRole role);

    Optional<AdminStation> findByProvinceAndCityAndStation(String province, String city, String station);
}

