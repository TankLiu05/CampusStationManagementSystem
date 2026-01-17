package com.campus.station.service.impl;

import com.campus.station.model.Parcel;
import com.campus.station.repository.ParcelRepository;
import com.campus.station.service.ParcelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
