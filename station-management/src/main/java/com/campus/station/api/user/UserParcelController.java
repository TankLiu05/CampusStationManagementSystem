package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Parcel;
import com.campus.station.service.ParcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "UserParcel", description = "用户快递查询接口")
@RequestMapping("/api/user/parcel")
public class UserParcelController {

    private final ParcelService service;
    private final com.campus.station.service.ParcelRouteService parcelRouteService;

    public UserParcelController(ParcelService service, com.campus.station.service.ParcelRouteService parcelRouteService) {
        this.service = service;
        this.parcelRouteService = parcelRouteService;
    }

    @GetMapping
    @Operation(summary = "查询我的快递列表")
    public ResponseEntity<?> listMyParcels(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Parcel> parcels = service.listByReceiver(currentUserId, pageable);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/search")
    @Operation(summary = "根据快递单号查询我的快递")
    public ResponseEntity<?> getMyParcelByTrackingNumber(@RequestParam String trackingNumber) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        return service.getByTrackingNumberAndReceiverId(trackingNumber, currentUserId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("未找到对应快递"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询快递详情")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        return service.getById(id)
                .map(parcel -> {
                    if (parcel.getReceiverId() != null && !parcel.getReceiverId().equals(currentUserId)) {
                         return ResponseEntity.status(403).body("无权查看此快递");
                    }
                    return ResponseEntity.ok((Object) parcel);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tracking/{trackingNumber}/routes")
    @Operation(summary = "查询快递流转记录")
    public ResponseEntity<?> getRoutesByTrackingNumber(@PathVariable String trackingNumber) {
        // Allow public access or restricted to logged-in user?
        // Requirement: "User side can see full process"
        // Assuming user needs to be logged in to access API, but maybe strict ownership check is not required for routes if they have the tracking number?
        // However, usually tracking is public if you have the number.
        // But for safety let's check login.
        
        // If strict ownership is needed:
        /*
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        // check ownership...
        */

        // For now, let's assume if they have the tracking number, they can see the routes (common practice), 
        // OR we can enforce they must be the receiver.
        // Given "User side can see full process", let's be permissive with tracking number query but maybe require login.
        
        // Let's implement strict check if user is receiver to be safe, or just login.
        // Re-reading user request: "User side can see full process...".
        // Let's assume login is required.
        
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        // Optional: Check if parcel belongs to user. 
        // If we want to allow querying ANY parcel by tracking number (like normal courier apps), we skip ownership check.
        // If we want to restrict to "My Parcels", we check receiverId.
        // User request: "User side can see full process".
        // Usually tracking search is open. Let's keep it open for logged in users.
        
        java.util.List<com.campus.station.model.ParcelRoute> routes = parcelRouteService.listByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(routes);
    }
}
