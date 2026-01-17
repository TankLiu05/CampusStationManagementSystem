package com.campus.station.api.admin;

import com.campus.station.common.PickupCodeUtil;
import com.campus.station.model.Parcel;
import com.campus.station.model.SysUser;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "AdminParcel", description = "管理员快递管理接口")
@RequestMapping("/api/admin/parcel")
public class AdminParcelController {

    private final ParcelService service;
    private final SysUserService sysUserService;

    public AdminParcelController(ParcelService service, SysUserService sysUserService) {
        this.service = service;
        this.sysUserService = sysUserService;
    }

    @PostMapping
    @Operation(summary = "创建快递（发货）")
    public ResponseEntity<?> create(@RequestBody AdminParcelCreateRequest req) {
        SysUser receiver = null;
        if (req.getReceiverUsername() != null && !req.getReceiverUsername().isBlank()) {
            receiver = sysUserService.getByUsername(req.getReceiverUsername()).orElse(null);
        }
        if (receiver == null && req.getReceiverPhone() != null && !req.getReceiverPhone().isBlank()) {
            receiver = sysUserService.getByPhone(req.getReceiverPhone()).orElse(null);
        }
        if (receiver == null) {
            return ResponseEntity.status(404).body("收件人用户不存在");
        }

        Parcel parcel = new Parcel();
        parcel.setTrackingNumber(req.getTrackingNumber());
        parcel.setCompany(req.getCompany());
        parcel.setReceiverId(receiver.getId());
        parcel.setReceiverName(req.getReceiverName() != null ? req.getReceiverName() : receiver.getUsername());
        parcel.setReceiverPhone(req.getReceiverPhone() != null ? req.getReceiverPhone() : receiver.getPhone());
        if (req.getStatus() != null) {
            parcel.setStatus(req.getStatus());
        }
        if (req.getIsSigned() != null) {
            parcel.setIsSigned(req.getIsSigned());
        }

        Parcel created = service.create(parcel);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/search")
    @Operation(summary = "根据快递单号查询快递")
    public ResponseEntity<?> getByTrackingNumber(@RequestParam String trackingNumber) {
        return service.getByTrackingNumber(trackingNumber)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("未找到对应快递"));
    }

    @PostMapping("/{id}/pickup")
    @Operation(summary = "为快递生成存放位置和取件码")
    public ResponseEntity<?> createPickupInfo(@PathVariable Long id) {
        return service.getById(id)
                .map(parcel -> {
                    if (parcel.getStatus() == null || parcel.getStatus() != 2) {
                        return ResponseEntity.status(400).body("快递未入库，不能创建取件信息");
                    }
                    if (parcel.getIsSigned() != null && parcel.getIsSigned() == 1) {
                        return ResponseEntity.status(400).body("快递已签收，不能创建取件信息");
                    }
                    if (parcel.getPickupCode() != null && !parcel.getPickupCode().isBlank()) {
                        return ResponseEntity.status(409).body("该快递已经存在取件码");
                    }
                    String pickupCode;
                    do {
                        pickupCode = PickupCodeUtil.generate();
                    } while (service.findActiveByPickupCode(pickupCode).isPresent());
                    String location;
                    do {
                        location = generateRandomLocation();
                    } while (service.findActiveByLocation(location).isPresent());

                    Parcel update = new Parcel();
                    update.setLocation(location);
                    update.setPickupCode(pickupCode);
                    Parcel updated = service.update(id, update);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static String generateRandomLocation() {
        char area = (char) ('A' + ThreadLocalRandom.current().nextInt(4));
        int shelf = ThreadLocalRandom.current().nextInt(1, 11);
        int code = ThreadLocalRandom.current().nextInt(0, 10000);
        String shelfPart = String.format("%02d货架", shelf);
        String codePart = String.format("%04d", code);
        return area + "区-" + shelfPart + "-" + codePart;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取快递")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询所有快递列表")
    public Page<Parcel> list(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return service.listByStatus(status, pageable);
        }
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新快递信息")
    public ResponseEntity<Parcel> update(@PathVariable Long id, @RequestBody Parcel req) {
        Parcel updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除快递")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "修改快递状态")
    public ResponseEntity<Parcel> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Parcel updated = service.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }

}
