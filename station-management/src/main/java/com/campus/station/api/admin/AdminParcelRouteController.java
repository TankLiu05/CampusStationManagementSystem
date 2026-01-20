package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelRoute;
import com.campus.station.model.SysAdmin;
import com.campus.station.service.ParcelRouteService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "AdminParcelRoute", description = "管理员快递流转记录接口")
@RequestMapping("/api/admin/parcelRoute")
public class AdminParcelRouteController {

    private final ParcelRouteService parcelRouteService;
    private final ParcelService parcelService;
    private final SysAdminService sysAdminService;

    public AdminParcelRouteController(ParcelRouteService parcelRouteService, ParcelService parcelService, SysAdminService sysAdminService) {
        this.parcelRouteService = parcelRouteService;
        this.parcelService = parcelService;
        this.sysAdminService = sysAdminService;
    }

    private SysAdmin requireAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }
        return current;
    }

    @PostMapping
    @Operation(summary = "创建快递流转记录")
    public ResponseEntity<?> create(@RequestBody AdminParcelRouteCreateRequest req) {
        requireAdmin();
        if (req.getTrackingNumber() == null || req.getTrackingNumber().isBlank()) {
            return ResponseEntity.badRequest().body("快递单号不能为空");
        }
        if (req.getCurrentStation() == null || req.getCurrentStation().isBlank()) {
            return ResponseEntity.badRequest().body("当前站点不能为空");
        }
        if (req.getNextStation() == null || req.getNextStation().isBlank()) {
            return ResponseEntity.badRequest().body("下一站点不能为空");
        }
        if (req.getEtaNextStation() == null) {
            return ResponseEntity.badRequest().body("预计到达下一站时间不能为空");
        }
        if (req.getEtaDelivered() == null) {
            return ResponseEntity.badRequest().body("预计送达时间不能为空");
        }

        Parcel parcel = parcelService.getByTrackingNumber(req.getTrackingNumber())
                .orElse(null);
        if (parcel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("快递不存在");
        }

        ParcelRoute route = new ParcelRoute();
        route.setTrackingNumber(req.getTrackingNumber());
        route.setCurrentStation(req.getCurrentStation());
        route.setNextStation(req.getNextStation());
        route.setEtaNextStation(req.getEtaNextStation());
        route.setEtaDelivered(req.getEtaDelivered());

        ParcelRoute created = parcelRouteService.create(route);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/etaDelivered")
    @Operation(summary = "修改快递预计送达时间")
    public ResponseEntity<?> updateEtaDelivered(@PathVariable Long id, @RequestBody AdminParcelRouteUpdateEtaRequest req) {
        requireAdmin();
        if (req.getEtaDelivered() == null) {
            return ResponseEntity.badRequest().body("预计送达时间不能为空");
        }
        ParcelRoute updated = parcelRouteService.updateEtaDelivered(id, req.getEtaDelivered());
        return ResponseEntity.ok(updated);
    }
}
