package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelRoute;
import com.campus.station.model.SysAdmin;
import com.campus.station.service.AdminRoleScopeService;
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

    private final AdminRoleScopeService adminRoleScopeService;

    public AdminParcelRouteController(ParcelRouteService parcelRouteService, ParcelService parcelService, SysAdminService sysAdminService, AdminRoleScopeService adminRoleScopeService) {
        this.parcelRouteService = parcelRouteService;
        this.parcelService = parcelService;
        this.sysAdminService = sysAdminService;
        this.adminRoleScopeService = adminRoleScopeService;
    }

    @org.springframework.web.bind.annotation.GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "根据快递单号查询流转记录")
    public ResponseEntity<?> getByTrackingNumber(@PathVariable String trackingNumber) {
        AdminRoleScope scope = requireCurrentAdminScope();
        
        Parcel parcel = parcelService.getByTrackingNumber(trackingNumber)
                .orElse(null);
        if (parcel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("快递不存在");
        }
        
        // Check visibility
        if (!parcelService.isParcelVisibleForScope(scope, parcel)) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权查看该快递的流转记录");
        }
        
        java.util.List<ParcelRoute> routes = parcelRouteService.listByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(routes);
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

    @PostMapping("/markDelivered/{trackingNumber}")
    @Operation(summary = "标记快递已送达")
    public ResponseEntity<?> markDelivered(@PathVariable String trackingNumber) {
        requireAdmin();
        try {
            // Check if parcel is already delivered
            java.util.List<ParcelRoute> routes = parcelRouteService.listByTrackingNumber(trackingNumber);
            if (!routes.isEmpty()) {
                ParcelRoute last = routes.get(routes.size() - 1);
                if (last.getIsDelivered() != null && last.getIsDelivered() == 1) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("该快递已送达，无需重复标记");
                }
            }

            parcelRouteService.markAsDelivered(trackingNumber);
            
            // Sync status to Parcel (Arrived = 2)
            java.util.List<Parcel> parcels = parcelService.getByTrackingNumber(trackingNumber)
                    .map(java.util.List::of)
                    .orElse(java.util.Collections.emptyList());
            if (!parcels.isEmpty()) {
                parcelService.changeStatus(parcels.get(0).getId(), 2);
            }
            
            return ResponseEntity.ok("操作成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("系统错误: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "创建快递流转记录")
    public ResponseEntity<?> create(@RequestBody AdminParcelRouteCreateRequest req) {
        requireAdmin();
        if (req.getTrackingNumber() == null || req.getTrackingNumber().isBlank()) {
            return ResponseEntity.badRequest().body("快递单号不能为空");
        }

        // Check if parcel is already delivered
        java.util.List<ParcelRoute> routes = parcelRouteService.listByTrackingNumber(req.getTrackingNumber());
        if (!routes.isEmpty()) {
            ParcelRoute last = routes.get(routes.size() - 1);
            if (last.getIsDelivered() != null && last.getIsDelivered() == 1) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("该快递已送达，无法继续更新物流信息");
            }
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

        // Check if parcel exists. If not, create it.
        Parcel parcel = parcelService.getByTrackingNumber(req.getTrackingNumber())
                .orElse(null);
        
        if (parcel == null) {
            // New parcel: validate required fields
            if (req.getCompany() == null || req.getCompany().isBlank()) {
                return ResponseEntity.badRequest().body("快递公司不能为空");
            }
            if (req.getOrigin() == null || req.getOrigin().isBlank()) {
                return ResponseEntity.badRequest().body("起始站不能为空");
            }
            if (req.getDestination() == null || req.getDestination().isBlank()) {
                return ResponseEntity.badRequest().body("终点站不能为空");
            }

            // Create new parcel with info from request
            parcel = new Parcel();
            parcel.setTrackingNumber(req.getTrackingNumber());
            parcel.setCompany(req.getCompany());
            parcel.setOrigin(req.getOrigin());
            parcel.setDestination(req.getDestination());
            // Other fields default
            parcel.setStatus(1); // Shipped
            parcel.setIsSigned(0);
            parcel.setReceiverName("Unknown"); // Placeholder
            parcel.setReceiverPhone("Unknown"); // Placeholder
            parcel = parcelService.create(parcel);
        } else {
             // Update existing parcel origin/destination/company if not set
             boolean changed = false;
             if (req.getCompany() != null && !req.getCompany().isBlank() && (parcel.getCompany() == null || parcel.getCompany().isBlank())) {
                 parcel.setCompany(req.getCompany());
                 changed = true;
             }
             if (req.getOrigin() != null && !req.getOrigin().isBlank() && (parcel.getOrigin() == null || parcel.getOrigin().isBlank())) {
                 parcel.setOrigin(req.getOrigin());
                 changed = true;
             }
             if (req.getDestination() != null && !req.getDestination().isBlank() && (parcel.getDestination() == null || parcel.getDestination().isBlank())) {
                 parcel.setDestination(req.getDestination());
                 changed = true;
             }
             if (changed) {
                 parcelService.update(parcel.getId(), parcel);
             }
        }

        AdminRoleScope scope = requireCurrentAdminScope();
        // 权限检查：如果是站点管理员，只能操作当前站点与自己站点一致的包裹
        if (scope.getRole() == com.campus.station.model.AdminRole.STREET_ADMIN) {
            String adminStation = scope.getStation();
            
            // 验证包裹实际位置是否在当前管理员站点
            String parcelLocation = parcel.getCurrentStation();
            if (parcelLocation == null || parcelLocation.isBlank()) {
                // 如果当前位置为空（如刚揽收），则检查发货地
                parcelLocation = parcel.getOrigin();
            }
            
            boolean hasPermission = false;
            
            // 1. 检查是否在当前站点（发货/处理中）
            if (adminStation != null && parcelLocation != null) {
                if (adminStation.equals(parcelLocation) || 
                    adminStation.contains(parcelLocation) || 
                    parcelLocation.contains(adminStation)) {
                    hasPermission = true;
                }
            }
            
            // 2. 检查是否是下一站（收货/待处理）
            // 如果当前管理员是包裹的“下一站”，则有权接收包裹并更新状态
            String nextStation = parcel.getNextStation();
            if (!hasPermission && adminStation != null && nextStation != null) {
                 if (adminStation.equals(nextStation) || 
                    adminStation.contains(nextStation) || 
                    nextStation.contains(adminStation)) {
                    hasPermission = true;
                }
            }

            if (!hasPermission) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您无权操作该包裹（非本站当前或待接收包裹）");
            }

            // 验证请求中的当前站点是否与管理员站点一致
            String reqStation = req.getCurrentStation();
            if (adminStation != null && reqStation != null) {
                boolean matchReq = adminStation.equals(reqStation) || 
                               adminStation.contains(reqStation) || 
                               reqStation.contains(adminStation);
                if (!matchReq) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您无权从 " + reqStation + " 发出包裹（非本站）");
                }
            }
        }

        ParcelRoute route = new ParcelRoute();
        route.setTrackingNumber(req.getTrackingNumber());
        route.setCurrentStation(req.getCurrentStation());
        route.setNextStation(req.getNextStation());
        route.setEtaNextStation(req.getEtaNextStation());
        route.setEtaDelivered(req.getEtaDelivered());
        route.setIsDelivered(req.getIsDelivered() != null ? req.getIsDelivered() : 0);

        ParcelRoute created = parcelRouteService.create(route);

        // 更新包裹的当前位置为最新的站点，同步物流信息
        Integer newStatus = null;
        // 如果包裹状态不是“运输中”且不是“已签收”，则更新为“运输中”
        if (parcel.getStatus() != 1 && parcel.getStatus() != 2) {
             newStatus = 1;
        }
        
        parcelService.updateLogisticsInfo(
            parcel.getId(),
            req.getCurrentStation(),
            created.getNextStation(),
            created.getEtaNextStation(),
            created.getEtaDelivered(),
            newStatus
        );

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
