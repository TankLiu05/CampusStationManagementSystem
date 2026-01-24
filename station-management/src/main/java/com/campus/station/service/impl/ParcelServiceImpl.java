package com.campus.station.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelRoute;
import com.campus.station.repository.ParcelRepository;
import com.campus.station.repository.ParcelRouteRepository;
import com.campus.station.service.ParcelService;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repository;
    private final ParcelRouteRepository parcelRouteRepository;
    private final com.campus.station.service.StationStorageService stationStorageService;

    public ParcelServiceImpl(ParcelRepository repository, 
                             ParcelRouteRepository parcelRouteRepository,
                             com.campus.station.service.StationStorageService stationStorageService) {
        this.repository = repository;
        this.parcelRouteRepository = parcelRouteRepository;
        this.stationStorageService = stationStorageService;
    }

    @Override
    @Transactional
    public Parcel create(Parcel parcel) {
        return repository.save(parcel);
    }

    @Override
    @Transactional
    public Parcel create(Parcel parcel, String initialStation) {
        if (initialStation != null && !initialStation.isBlank()) {
            parcel.setCurrentStation(initialStation);
        }
        Parcel saved = repository.save(parcel);
        if (initialStation != null && !initialStation.isBlank()) {
            ParcelRoute route = new ParcelRoute();
            route.setTrackingNumber(saved.getTrackingNumber());
            route.setCurrentStation(initialStation);
            parcelRouteRepository.save(route);
        }
        return saved;
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
        if (update.getCurrentStation() != null) {
            existing.setCurrentStation(update.getCurrentStation());
        }
        if (update.getNextStation() != null) {
            existing.setNextStation(update.getNextStation());
        }
        if (update.getEtaNextStation() != null) {
            existing.setEtaNextStation(update.getEtaNextStation());
        }
        if (update.getEtaDelivered() != null) {
            existing.setEtaDelivered(update.getEtaDelivered());
        }
        if (update.getPickupCode() != null) {
            existing.setPickupCode(update.getPickupCode());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        if (update.getIsSigned() != null) {
            existing.setIsSigned(update.getIsSigned());
            stationStorageService.updateSignStatus(existing.getTrackingNumber(), update.getIsSigned());
        }
        if (update.getIsReturned() != null) {
            existing.setIsReturned(update.getIsReturned());
        }

        Parcel saved = repository.save(existing);
        
        // 当更新包裹信息且有取件码和位置时，同步到仓库
        if (saved.getPickupCode() != null && saved.getLocation() != null) {
            stationStorageService.syncFromParcel(saved);
        }
        
        return saved;
    }

    @Override
    @Transactional
    public void updateLogisticsInfo(Long id, String currentStation, String nextStation, java.time.LocalDateTime etaNextStation, java.time.LocalDateTime etaDelivered, Integer status) {
        Parcel existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("快递不存在"));
        
        if (currentStation != null) {
            existing.setCurrentStation(currentStation);
        }
        // nextStation 允许为 null (表示已到达某站，暂无下一站)
        existing.setNextStation(nextStation);
        existing.setEtaNextStation(etaNextStation);
        
        if (etaDelivered != null) {
            existing.setEtaDelivered(etaDelivered);
        }
        if (status != null) {
            existing.setStatus(status);
        }
        
        repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.findById(id).ifPresent(parcel -> {
            stationStorageService.deleteByTrackingNumber(parcel.getTrackingNumber());
            repository.delete(parcel);
        });
    }

    @Override
    @Transactional
    public void deleteBatch(Iterable<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
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
        
        stationStorageService.updateSignStatus(existing.getTrackingNumber(), 1);
        
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
        String stationCode = scope.getStation();

        if (role == AdminRole.MANAGER) {
            return matchesProvince(parcel, province);
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
        
        province = province.trim();
        
        // 移除“省”后缀进行宽松匹配，解决“河北省”匹配不到“河北xxx”的问题
        String cleanProvince = province.endsWith("省") ? province.substring(0, province.length() - 1) : province;

        return checkLocationMatch(parcel, province, cleanProvince);
    }

    private boolean matchesCity(Parcel parcel, String city) {
        if (city == null || city.isBlank()) {
            return false;
        }
        
        city = city.trim();
        
        // 移除“市”后缀进行宽松匹配
        String cleanCity = city.endsWith("市") ? city.substring(0, city.length() - 1) : city;

        return checkLocationMatch(parcel, city, cleanCity);
    }

    private boolean checkLocationMatch(Parcel parcel, String location, String cleanLocation) {
        String origin = parcel.getOrigin();
        String destination = parcel.getDestination();
        
        if (origin != null && (origin.contains(location) || origin.contains(cleanLocation))) {
            return true;
        }
        if (destination != null && (destination.contains(location) || destination.contains(cleanLocation))) {
            return true;
        }
        List<ParcelRoute> routes = parcelRouteRepository
                .findByTrackingNumberOrderByCreateTimeAsc(parcel.getTrackingNumber());
        for (ParcelRoute route : routes) {
            String current = route.getCurrentStation();
            String next = route.getNextStation();
            if (current != null && (current.contains(location) || current.contains(cleanLocation))) {
                return true;
            }
            if (next != null && (next.contains(location) || next.contains(cleanLocation))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Page<Parcel> listByReceiverPhone(String receiverPhone, Pageable pageable) {
        return repository.findByReceiverPhone(receiverPhone, pageable);
    }
    
    @Override
    public Optional<Parcel> getByTrackingNumberAndReceiverPhone(String trackingNumber, String receiverPhone) {
        return repository.findByTrackingNumberAndReceiverPhone(trackingNumber, receiverPhone);
    }

    @Override
    public Page<Parcel> listByReceiverIdOrReceiverPhone(Long receiverId, String receiverPhone, Pageable pageable) {
        return repository.findByReceiverIdOrReceiverPhone(receiverId, receiverPhone, pageable);
    }
}
