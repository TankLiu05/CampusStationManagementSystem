package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.StationStorage;
import com.campus.station.model.SysAdmin;
import com.campus.station.service.AdminRoleScopeService;
import com.campus.station.service.StationStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/station-storage")
@Tag(name = "Admin Station Storage", description = "管理员快递仓库管理")
public class AdminStationStorageController {

    private final StationStorageService stationStorageService;
    private final AdminRoleScopeService adminRoleScopeService;

    public AdminStationStorageController(StationStorageService stationStorageService, AdminRoleScopeService adminRoleScopeService) {
        this.stationStorageService = stationStorageService;
        this.adminRoleScopeService = adminRoleScopeService;
    }

    private SysAdmin requireAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }
        return current;
    }

    private AdminRoleScope requireCurrentAdminScope() {
        SysAdmin admin = requireAdmin();
        return adminRoleScopeService.getByAdminId(admin.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员角色未配置"));
    }

    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "根据快递单号查询仓库信息")
    public ResponseEntity<?> getByTrackingNumber(@PathVariable String trackingNumber) {
        requireAdmin();
        return stationStorageService.getByTrackingNumber(trackingNumber)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到该快递的仓库信息"));
    }

    @GetMapping("/search")
    @Operation(summary = "多条件查询仓库库存")
    public ResponseEntity<?> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String area,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String shelf,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String position,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String receiverName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String receiverPhone) {
        AdminRoleScope scope = requireCurrentAdminScope();
        String stationName = scope.getStation();
        // Only STREET_ADMIN has a specific station. Higher admins might have null (see all) or region.
        // For simplicity, if station is set, filter by it.
        // Note: scope.getStation() returns the station name string.
        
        return ResponseEntity.ok(stationStorageService.search(stationName, area, shelf, position, receiverName, receiverPhone));
    }
}
