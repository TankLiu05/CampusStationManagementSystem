package com.campus.station.service.impl;

import com.campus.station.model.Parcel;
import com.campus.station.model.StationStorage;
import com.campus.station.repository.StationStorageRepository;
import com.campus.station.service.StationStorageService;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StationStorageServiceImpl implements StationStorageService {

    private final StationStorageRepository repository;
    // Regex to parse location: e.g., "A区-1号货架-1234" or "A区-01货架-1234"
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^([A-D])区-(\\d+)(?:号)?货架-(\\d{4})$");

    public StationStorageServiceImpl(StationStorageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public StationStorage syncFromParcel(Parcel parcel) {
        StationStorage storage = repository.findByTrackingNumber(parcel.getTrackingNumber())
                .orElse(new StationStorage());

        storage.setTrackingNumber(parcel.getTrackingNumber());
        storage.setReceiverName(parcel.getReceiverName());
        storage.setReceiverPhone(parcel.getReceiverPhone());
        storage.setPickupCode(parcel.getPickupCode());
        storage.setIsSigned(parcel.getIsSigned());

        // Parse location string
        String location = parcel.getLocation();
        if (location != null) {
            Matcher matcher = LOCATION_PATTERN.matcher(location);
            if (matcher.find()) {
                storage.setArea(matcher.group(1));
                storage.setShelf(matcher.group(2));
                storage.setPosition(matcher.group(3));
            }
        }

        return repository.save(storage);
    }

    @Override
    @Transactional
    public void updateSignStatus(String trackingNumber, Integer isSigned) {
        repository.findByTrackingNumber(trackingNumber).ifPresent(storage -> {
            storage.setIsSigned(isSigned);
            repository.save(storage);
        });
    }

    @Override
    public Optional<StationStorage> getByTrackingNumber(String trackingNumber) {
        return repository.findByTrackingNumber(trackingNumber);
    }

    @Override
    @Transactional
    public void deleteByTrackingNumber(String trackingNumber) {
        repository.findByTrackingNumber(trackingNumber).ifPresent(repository::delete);
    }

    @Override
    public java.util.List<StationStorage> search(String area, String shelf, String position, String receiverName, String receiverPhone) {
        return repository.findAll((org.springframework.data.jpa.domain.Specification<StationStorage>) (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (area != null && !area.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("area"), area));
            }
            if (shelf != null && !shelf.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("shelf"), shelf));
            }
            if (position != null && !position.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("position"), position));
            }
            if (receiverName != null && !receiverName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("receiverName"), "%" + receiverName + "%"));
            }
            if (receiverPhone != null && !receiverPhone.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("receiverPhone"), "%" + receiverPhone + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }
}
