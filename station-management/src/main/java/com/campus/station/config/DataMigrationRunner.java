package com.campus.station.config;

import com.campus.station.model.Parcel;
import com.campus.station.model.StationStorage;
import com.campus.station.repository.ParcelRepository;
import com.campus.station.repository.StationStorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataMigrationRunner.class);

    private final StationStorageRepository stationStorageRepository;
    private final ParcelRepository parcelRepository;

    public DataMigrationRunner(StationStorageRepository stationStorageRepository, ParcelRepository parcelRepository) {
        this.stationStorageRepository = stationStorageRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting data migration for StationStorage stationName...");
        
        List<StationStorage> storages = stationStorageRepository.findByStationNameIsNull();
        if (storages.isEmpty()) {
            logger.info("No StationStorage records found with null stationName.");
            return;
        }

        int updatedCount = 0;
        for (StationStorage storage : storages) {
            Optional<Parcel> parcelOpt = parcelRepository.findByTrackingNumber(storage.getTrackingNumber());
            if (parcelOpt.isPresent()) {
                Parcel parcel = parcelOpt.get();
                if (parcel.getCurrentStation() != null) {
                    storage.setStationName(parcel.getCurrentStation());
                    // Update other fields if necessary to ensure consistency
                    if (storage.getReceiverName() == null) storage.setReceiverName(parcel.getReceiverName());
                    if (storage.getReceiverPhone() == null) storage.setReceiverPhone(parcel.getReceiverPhone());
                    if (storage.getIsSigned() == null) storage.setIsSigned(parcel.getIsSigned());
                    
                    stationStorageRepository.save(storage);
                    updatedCount++;
                }
            }
        }

        logger.info("Completed data migration. Updated {} StationStorage records.", updatedCount);
    }
}
