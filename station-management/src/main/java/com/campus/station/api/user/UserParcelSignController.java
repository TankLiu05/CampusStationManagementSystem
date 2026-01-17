package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Parcel;
import com.campus.station.service.ParcelStorageService;
import com.campus.station.service.ParcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UserParcelSign", description = "用户快递签收接口")
@RequestMapping("/api/user/parcel")
public class UserParcelSignController {

    private final ParcelService service;
    private final ParcelStorageService storageService;

    public UserParcelSignController(ParcelService service, ParcelStorageService storageService) {
        this.service = service;
        this.storageService = storageService;
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "签收快递")
    public ResponseEntity<?> sign(@PathVariable Long id) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        return service.getById(id)
                .map(parcel -> {
                    if (parcel.getReceiverId() != null && !parcel.getReceiverId().equals(currentUserId)) {
                        return ResponseEntity.status(403).body("无权操作此快递");
                    }
                    if (parcel.getIsSigned() != null && parcel.getIsSigned() == 1) {
                        return ResponseEntity.status(400).body("快递已签收，无法再次修改");
                    }
                    storageService.markSignedByParcelId(id);
                    Parcel updated = service.markSigned(id);
                    return ResponseEntity.ok((Object) updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/signed")
    @Operation(summary = "查看我已签收的快递列表")
    public ResponseEntity<?> listSigned(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Parcel> parcels = service.listByReceiverAndIsSigned(currentUserId, 1, pageable);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/unsigned")
    @Operation(summary = "查看我未签收的快递列表")
    public ResponseEntity<?> listUnsigned(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = SessionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Parcel> parcels = service.listByReceiverAndIsSigned(currentUserId, 0, pageable);
        return ResponseEntity.ok(parcels);
    }
}
