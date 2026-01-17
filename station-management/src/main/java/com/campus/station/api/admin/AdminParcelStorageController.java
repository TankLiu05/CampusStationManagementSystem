package com.campus.station.api.admin;

import com.campus.station.common.PickupCodeUtil;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelStorage;
import com.campus.station.service.ParcelService;
import com.campus.station.service.ParcelStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "AdminParcelStorage", description = "管理员快递到件记录管理接口")
@RequestMapping("/api/admin/parcel")
public class AdminParcelStorageController {

    private final ParcelService parcelService;
    private final ParcelStorageService parcelStorageService;

    public AdminParcelStorageController(ParcelService parcelService,
                                        ParcelStorageService parcelStorageService) {
        this.parcelService = parcelService;
        this.parcelStorageService = parcelStorageService;
    }

    @PostMapping("/{id}/storage")
    @Operation(summary = "创建快递到件记录和取件码")
    public ResponseEntity<?> createStorage(@PathVariable Long id, @RequestBody AdminParcelStorageRequest req) {
        if (req.getLocation() == null || req.getLocation().isBlank()) {
            return ResponseEntity.badRequest().body("存放位置为必填项");
        }
        Parcel parcel = parcelService.getById(id).orElse(null);
        if (parcel == null) {
            return ResponseEntity.notFound().build();
        }
        if (parcel.getIsSigned() != null && parcel.getIsSigned() == 1) {
            return ResponseEntity.status(400).body("快递已签收，不能创建到件记录");
        }
        if (parcelStorageService.getByParcelId(id).isPresent()) {
            return ResponseEntity.status(409).body("该快递已经存在到件记录");
        }
        String pickupCode;
        do {
            pickupCode = PickupCodeUtil.generate();
        } while (parcelStorageService.findActiveByPickupCode(pickupCode).isPresent());
        ParcelStorage storage = parcelStorageService.createForParcel(parcel, pickupCode, req.getLocation());
        parcelService.changeStatus(id, 2);
        return ResponseEntity.ok(storage);
    }

    @GetMapping("/storage")
    @Operation(summary = "分页查询快递到件记录列表")
    public Page<ParcelStorage> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return parcelStorageService.list(pageable);
    }

    @GetMapping("/storage/{storageId}")
    @Operation(summary = "根据到件记录ID获取详情")
    public ResponseEntity<?> getById(@PathVariable Long storageId) {
        return parcelStorageService.getById(storageId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/storage/{storageId}")
    @Operation(summary = "更新到件记录信息")
    public ResponseEntity<ParcelStorage> update(@PathVariable Long storageId, @RequestBody ParcelStorage update) {
        ParcelStorage updated = parcelStorageService.update(storageId, update);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/storage/{storageId}")
    @Operation(summary = "删除到件记录")
    public ResponseEntity<Void> delete(@PathVariable Long storageId) {
        parcelStorageService.delete(storageId);
        return ResponseEntity.noContent().build();
    }
}

