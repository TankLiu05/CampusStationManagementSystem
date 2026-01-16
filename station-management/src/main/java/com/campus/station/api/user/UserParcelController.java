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

    public UserParcelController(ParcelService service) {
        this.service = service;
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
                    // 简单的权限校验：只能看自己的
                    if (parcel.getReceiverId() != null && !parcel.getReceiverId().equals(currentUserId)) {
                         return ResponseEntity.status(403).body("无权查看此快递");
                    }
                    return ResponseEntity.ok((Object) parcel);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "签收快递")
    public ResponseEntity<?> sign(@PathVariable Long id) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
             return ResponseEntity.status(401).body("未登录");
        }
        
        // 先检查是否存在且归属
        return service.getById(id)
                .map(parcel -> {
                    if (parcel.getReceiverId() != null && !parcel.getReceiverId().equals(currentUserId)) {
                         return ResponseEntity.status(403).body("无权操作此快递");
                    }
                    if (parcel.getIsSigned() != null && parcel.getIsSigned() == 1) {
                         return ResponseEntity.status(400).body("快递已签收，无法再次修改");
                    }
                    Parcel updated = service.markSigned(id);
                    return ResponseEntity.ok((Object) updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
