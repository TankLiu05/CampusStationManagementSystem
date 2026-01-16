package com.campus.station.api.admin;

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
@Tag(name = "AdminParcel", description = "管理员快递管理接口")
@RequestMapping("/api/admin/parcel")
public class AdminParcelController {

    private final ParcelService service;

    public AdminParcelController(ParcelService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "创建快递（发货）")
    public ResponseEntity<Parcel> create(@RequestBody Parcel req) {
        Parcel created = service.create(req);
        return ResponseEntity.ok(created);
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
                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
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
