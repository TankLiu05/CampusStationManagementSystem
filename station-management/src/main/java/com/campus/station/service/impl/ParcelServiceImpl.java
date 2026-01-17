package com.campus.station.service.impl;

import com.campus.station.model.Parcel;
import com.campus.station.repository.ParcelRepository;
import com.campus.station.service.ParcelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repository;

    public ParcelServiceImpl(ParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Parcel create(Parcel parcel) {
        return repository.save(parcel);
    }

    @Override
    public Optional<Parcel> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Parcel> getByTrackingNumber(String trackingNumber) {
        return repository.findByTrackingNumber(trackingNumber);
    }

    @Override
    public Page<Parcel> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<Parcel> listByStatus(Integer status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public Parcel update(Long id, Parcel update) {
        Parcel existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("快递不存在"));

        if (update.getTrackingNumber() != null) {
            existing.setTrackingNumber(update.getTrackingNumber());
        }
        if (update.getCompany() != null) {
            existing.setCompany(update.getCompany());
        }
        if (update.getReceiverId() != null) {
            existing.setReceiverId(update.getReceiverId());
        }
        if (update.getReceiverName() != null) {
            existing.setReceiverName(update.getReceiverName());
        }
        if (update.getReceiverPhone() != null) {
            existing.setReceiverPhone(update.getReceiverPhone());
        }
        if (update.getLocation() != null) {
            existing.setLocation(update.getLocation());
        }
        if (update.getPickupCode() != null) {
            existing.setPickupCode(update.getPickupCode());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        if (update.getIsSigned() != null) {
            existing.setIsSigned(update.getIsSigned());
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
    public void deleteBatch(Iterable<Long> ids) {
        repository.deleteAllById(ids);
    }

    @Override
    @Transactional
    public Parcel changeStatus(Long id, Integer status) {
        Parcel existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("快递不存在"));
        existing.setStatus(status);
        return repository.save(existing);
    }

    @Override
    @Transactional
    public Parcel markSigned(Long id) {
        Parcel existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("快递不存在"));
        existing.setIsSigned(1);
        existing.setStatus(2);
        return repository.save(existing);
    }

    @Override
    public Page<Parcel> listByReceiver(Long receiverId, Pageable pageable) {
        return repository.findByReceiverId(receiverId, pageable);
    }

    @Override
    public Optional<Parcel> getByTrackingNumberAndReceiverId(String trackingNumber, Long receiverId) {
        return repository.findByTrackingNumberAndReceiverId(trackingNumber, receiverId);
    }

    @Override
    public Page<Parcel> listByReceiverAndIsSigned(Long receiverId, Integer isSigned, Pageable pageable) {
        return repository.findByReceiverIdAndIsSigned(receiverId, isSigned, pageable);
    }

    @Override
    public Optional<Parcel> findActiveByPickupCode(String pickupCode) {
        return repository.findByPickupCodeAndIsSigned(pickupCode, 0);
    }

    @Override
    public Optional<Parcel> findActiveByLocation(String location) {
        return repository.findByLocationAndIsSigned(location, 0);
    }

    @Override
    @Transactional
    public void updateReceiverInfo(Long receiverId, String receiverName, String receiverPhone) {
        List<Parcel> parcels = repository.findByReceiverId(receiverId);
        if (parcels.isEmpty()) {
            return;
        }
        for (Parcel parcel : parcels) {
            if (receiverName != null && !receiverName.isBlank()) {
                parcel.setReceiverName(receiverName);
            }
            if (receiverPhone != null && !receiverPhone.isBlank()) {
                parcel.setReceiverPhone(receiverPhone);
            }
        }
        repository.saveAll(parcels);
    }
}
