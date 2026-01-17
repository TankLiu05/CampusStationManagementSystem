package com.campus.station.service.impl;

import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelStorage;
import com.campus.station.repository.ParcelStorageRepository;
import com.campus.station.service.ParcelStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ParcelStorageServiceImpl implements ParcelStorageService {

    private final ParcelStorageRepository repository;

    public ParcelStorageServiceImpl(ParcelStorageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ParcelStorage createForParcel(Parcel parcel, String pickupCode, String location) {
        ParcelStorage storage = new ParcelStorage();
        storage.setParcelId(parcel.getId());
        storage.setTrackingNumber(parcel.getTrackingNumber());
        storage.setPickupCode(pickupCode);
        storage.setLocation(location);
        storage.setReceiverName(parcel.getReceiverName());
        storage.setReceiverPhone(parcel.getReceiverPhone());
        return repository.save(storage);
    }

    @Override
    public Optional<ParcelStorage> getByParcelId(Long parcelId) {
        return repository.findByParcelId(parcelId);
    }

    @Override
    public Optional<ParcelStorage> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<ParcelStorage> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public ParcelStorage update(Long id, ParcelStorage update) {
        ParcelStorage existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("到件记录不存在"));
        if (update.getLocation() != null) {
            existing.setLocation(update.getLocation());
        }
        if (update.getPickupCode() != null) {
            existing.setPickupCode(update.getPickupCode());
        }
        if (update.getReceiverName() != null) {
            existing.setReceiverName(update.getReceiverName());
        }
        if (update.getReceiverPhone() != null) {
            existing.setReceiverPhone(update.getReceiverPhone());
        }
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void markSignedByParcelId(Long parcelId) {
        repository.findByParcelId(parcelId).ifPresent(storage -> {
            if (storage.getSignedTime() == null) {
                storage.setSignedTime(LocalDateTime.now());
                repository.save(storage);
            }
        });
    }

    @Override
    public Optional<ParcelStorage> findActiveByPickupCode(String pickupCode) {
        return repository.findByPickupCodeAndSignedTimeIsNull(pickupCode);
    }
}
