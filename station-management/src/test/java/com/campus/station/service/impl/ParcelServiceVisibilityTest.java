package com.campus.station.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelRoute;
import com.campus.station.repository.ParcelRepository;
import com.campus.station.repository.ParcelRouteRepository;
import com.campus.station.service.StationStorageService;

public class ParcelServiceVisibilityTest {

    private ParcelServiceImpl parcelService;
    private ParcelRepository parcelRepository;
    private ParcelRouteRepository parcelRouteRepository;
    private StationStorageService stationStorageService;

    @BeforeEach
    void setUp() {
        parcelRepository = mock(ParcelRepository.class);
        parcelRouteRepository = mock(ParcelRouteRepository.class);
        stationStorageService = mock(StationStorageService.class);
        parcelService = new ParcelServiceImpl(parcelRepository, parcelRouteRepository, stationStorageService);
    }

    @Test
    void testParcelVisibleForStationWithRoute() {
        // Arrange
        String stationName = "Hebei Shijiazhuang Station A";
        String trackingNumber = "T123";
        
        Parcel parcel = new Parcel();
        parcel.setTrackingNumber(trackingNumber);
        parcel.setOrigin("Some Other Place"); // Origin does NOT match station

        ParcelRoute route = new ParcelRoute();
        route.setTrackingNumber(trackingNumber);
        route.setCurrentStation(stationName); // Route matches station

        List<ParcelRoute> routes = new ArrayList<>();
        routes.add(route);

        when(parcelRouteRepository.findByTrackingNumberOrderByCreateTimeAsc(trackingNumber)).thenReturn(routes);

        // Act
        boolean isVisible = parcelService.isParcelVisibleForStation(parcel, stationName);

        // Assert
        assertTrue(isVisible, "Parcel should be visible if a route exists at the station");
    }

    @Test
    void testParcelVisibleForManagerWithRoute() {
        // Arrange
        String province = "Hebei";
        String stationName = "Hebei Shijiazhuang Station A";
        String trackingNumber = "T123";
        
        Parcel parcel = new Parcel();
        parcel.setTrackingNumber(trackingNumber);
        parcel.setOrigin("Beijing"); // Origin does NOT match province

        ParcelRoute route = new ParcelRoute();
        route.setTrackingNumber(trackingNumber);
        route.setCurrentStation(stationName); // Route contains province

        List<ParcelRoute> routes = new ArrayList<>();
        routes.add(route);

        when(parcelRouteRepository.findByTrackingNumberOrderByCreateTimeAsc(trackingNumber)).thenReturn(routes);

        AdminRoleScope scope = new AdminRoleScope();
        scope.setRole(AdminRole.MANAGER);
        scope.setProvince(province);

        // Act
        boolean isVisible = parcelService.isParcelVisibleForScope(scope, parcel);

        // Assert
        assertTrue(isVisible, "Parcel should be visible to Manager if route is in province");
    }
}
