package com.campus.station.service.impl;

import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelRoute;
import com.campus.station.repository.ParcelRepository;
import com.campus.station.repository.ParcelRouteRepository;
import com.campus.station.service.ParcelService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repository;
    private final ParcelRouteRepository parcelRouteRepository;

    public ParcelServiceImpl(ParcelRepository repository, ParcelRouteRepository parcelRouteRepository) {
        this.repository = repository;
        this.parcelRouteRepository = parcelRouteRepository;
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
    public Page<Parcel> listByStation(String stationCode, Pageable pageable) {
        List<Parcel> all = repository.findAll();
        List<Parcel> filtered = all.stream()
                .filter(parcel -> isParcelVisibleForStation(parcel, stationCode))
                .collect(Collectors.toList());
        return toPage(filtered, pageable);
    }

    @Override
    public Page<Parcel> listByStationAndStatus(String stationCode, Integer status, Pageable pageable) {
        List<Parcel> all = repository.findByStatus(status, Pageable.unpaged()).getContent();
        List<Parcel> filtered = all.stream()
                .filter(parcel -> isParcelVisibleForStation(parcel, stationCode))
                .collect(Collectors.toList());
        return toPage(filtered, pageable);
    }

    @Override
    public Page<Parcel> listForScope(AdminRoleScope scope, Pageable pageable) {
        List<Parcel> all = repository.findAll();
        List<Parcel> filtered = all.stream()
                .filter(parcel -> isParcelVisibleForScope(scope, parcel))
                .collect(Collectors.toList());
        return toPage(filtered, pageable);
    }

    @Override
    public Page<Parcel> listForScopeAndStatus(AdminRoleScope scope, Integer status, Pageable pageable) {
        List<Parcel> all = repository.findByStatus(status, Pageable.unpaged()).getContent();
        List<Parcel> filtered = all.stream()
                .filter(parcel -> isParcelVisibleForScope(scope, parcel))
                .collect(Collectors.toList());
        return toPage(filtered, pageable);
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
        if (update.getOrigin() != null) {
            existing.setOrigin(update.getOrigin());
        }
        if (update.getDestination() != null) {
            existing.setDestination(update.getDestination());
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

    @Override
    public boolean isParcelVisibleForStation(Parcel parcel, String stationCode) {
        if (stationCode == null || stationCode.isBlank()) {
            return true;
        }
        if (stationCode.equals(parcel.getOrigin())) {
            return true;
        }
        List<ParcelRoute> routes = parcelRouteRepository.findByTrackingNumberOrderByCreateTimeAsc(parcel.getTrackingNumber());
        if (routes.isEmpty()) {
            return false;
        }
        for (ParcelRoute route : routes) {
            if (stationCode.equals(route.getCurrentStation()) || stationCode.equals(route.getNextStation())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isParcelVisibleForScope(AdminRoleScope scope, Parcel parcel) {
        if (scope == null) {
            return false;
        }
        AdminRole role = scope.getRole();
        if (role == AdminRole.SUPERADMIN) {
            return true;
        }

        String province = scope.getProvince();
        String city = scope.getCity();
        String stationCode = scope.getStationId() == null ? null : String.valueOf(scope.getStationId());

        if (role == AdminRole.MANAGER) {
            return matchesProvince(parcel, province);
        }

        if (role == AdminRole.CITY_ADMIN) {
            return matchesCity(parcel, city);
        }

        if (role == AdminRole.STREET_ADMIN) {
            return isParcelVisibleForStation(parcel, stationCode);
        }

        return false;
    }

    private Page<Parcel> toPage(List<Parcel> items, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(items);
        }
        int total = items.size();
        int start = (int) pageable.getOffset();
        if (start >= total) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }
        int end = Math.min(start + pageable.getPageSize(), total);
        List<Parcel> content = items.subList(start, end);
        return new PageImpl<>(content, pageable, total);
    }

    private boolean matchesProvince(Parcel parcel, String province) {
        if (province == null || province.isBlank()) {
            return false;
        }
        String origin = parcel.getOrigin();
        String destination = parcel.getDestination();
        if (origin != null && origin.contains(province)) {
            return true;
        }
        if (destination != null && destination.contains(province)) {
            return true;
        }
        List<ParcelRoute> routes = parcelRouteRepository
                .findByTrackingNumberOrderByCreateTimeAsc(parcel.getTrackingNumber());
        for (ParcelRoute route : routes) {
            String current = route.getCurrentStation();
            String next = route.getNextStation();
            if (current != null && current.contains(province)) {
                return true;
            }
            if (next != null && next.contains(province)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCity(Parcel parcel, String city) {
        if (city == null || city.isBlank()) {
            return false;
        }
        String origin = parcel.getOrigin();
        String destination = parcel.getDestination();
        if (origin != null && origin.contains(city)) {
            return true;
        }
        if (destination != null && destination.contains(city)) {
            return true;
        }
        List<ParcelRoute> routes = parcelRouteRepository
                .findByTrackingNumberOrderByCreateTimeAsc(parcel.getTrackingNumber());
        for (ParcelRoute route : routes) {
            String current = route.getCurrentStation();
            String next = route.getNextStation();
            if (current != null && current.contains(city)) {
                return true;
            }
            if (next != null && next.contains(city)) {
                return true;
            }
        }
        return false;
    }
}
