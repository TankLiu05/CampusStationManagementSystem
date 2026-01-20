package com.campus.station.repository;

import com.campus.station.model.AdminStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminStationRepository extends JpaRepository<AdminStation, Long> {

    boolean existsByProvinceAndCityAndStation(String province, String city, String station);
}

