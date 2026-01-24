package com.campus.station.api.admin;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import com.campus.station.common.PickupCodeUtil;
import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import com.campus.station.model.ParcelRoute;
import com.campus.station.model.SysAdmin;
import com.campus.station.model.SysUser;
import com.campus.station.service.AdminRoleScopeService;
import com.campus.station.service.ParcelRouteService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysAdminService;
import com.campus.station.service.SysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "AdminParcel", description = "管理员快递管理接口")
@RequestMapping("/api/admin/parcel")
public class AdminParcelController {

    private final ParcelService service;
    private final ParcelRouteService parcelRouteService;
    private final SysUserService sysUserService;
    private final SysAdminService sysAdminService;
    private final AdminRoleScopeService adminRoleScopeService;
    private final com.campus.station.service.StationStorageService stationStorageService;

    public AdminParcelController(ParcelService service,
                                 ParcelRouteService parcelRouteService,
                                 SysUserService sysUserService,
                                 SysAdminService sysAdminService,
                                 AdminRoleScopeService adminRoleScopeService,
                                 com.campus.station.service.StationStorageService stationStorageService) {
        this.service = service;
        this.parcelRouteService = parcelRouteService;
        this.sysUserService = sysUserService;
        this.sysAdminService = sysAdminService;
        this.adminRoleScopeService = adminRoleScopeService;
        this.stationStorageService = stationStorageService;
    }

    private SysAdmin requireCurrentAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }
        return current;
    }

    private AdminRoleScope requireCurrentAdminScope() {
        SysAdmin admin = requireCurrentAdmin();
        return adminRoleScopeService.getByAdminId(admin.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员角色未配置"));
    }

    @PostMapping
    @Operation(summary = "创建快递（发货）")
    public ResponseEntity<?> create(@RequestBody AdminParcelCreateRequest req) {
        AdminRoleScope scope = requireCurrentAdminScope();
        
        // 验证必填字段
        if (req.getReceiverPhone() == null || req.getReceiverPhone().isBlank()) {
            return ResponseEntity.badRequest().body("收件人手机号不能为空");
        }
        
        // 尝试通过用户名或手机号查找系统用户（如果已注册，则关联用户ID）
        SysUser receiver = null;
        if (req.getReceiverUsername() != null && !req.getReceiverUsername().isBlank()) {
            receiver = sysUserService.getByUsername(req.getReceiverUsername()).orElse(null);
        }
        if (receiver == null && req.getReceiverPhone() != null && !req.getReceiverPhone().isBlank()) {
            receiver = sysUserService.getByPhone(req.getReceiverPhone()).orElse(null);
        }
        
        Parcel parcel = new Parcel();
        
        // 自动生成快递单号（如果未提供）
        String trackingNumber = req.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = generateTrackingNumber();
        }
        parcel.setTrackingNumber(trackingNumber);
        
        parcel.setCompany(req.getCompany());
        
        // 如果找到了系统用户，则关联用户ID；否则receiverId为null
        if (receiver != null) {
            parcel.setReceiverId(receiver.getId());
        }
        
        // 收件人姓名和手机号直接使用表单提供的值
        parcel.setReceiverName(req.getReceiverName() != null && !req.getReceiverName().isBlank() 
                ? req.getReceiverName() 
                : (receiver != null ? receiver.getUsername() : "未填写"));
        parcel.setReceiverPhone(req.getReceiverPhone());
        parcel.setOrigin(req.getOrigin());
        parcel.setDestination(req.getDestination());
        if (req.getStatus() != null) {
            parcel.setStatus(req.getStatus());
        }
        if (req.getIsSigned() != null) {
            parcel.setIsSigned(req.getIsSigned());
        }
        if (req.getIsReturned() != null) {
            parcel.setIsReturned(req.getIsReturned());
        }

        Parcel created = service.create(parcel, scope.getStation());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/generate-tracking-number")
    @Operation(summary = "生成快递单号")
    public ResponseEntity<String> generateTrackingNumberApi() {
        requireCurrentAdmin();
        String trackingNumber = generateTrackingNumber();
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("\""+trackingNumber+"\""); // 返回JSON格式的字符串
    }

    @GetMapping("/search")
    @Operation(summary = "根据快递单号查询快递")
    public ResponseEntity<?> getByTrackingNumber(@RequestParam String trackingNumber) {
        AdminRoleScope scope = requireCurrentAdminScope();
        return service.getByTrackingNumber(trackingNumber)
                .filter(parcel -> service.isParcelVisibleForScope(scope, parcel))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("未找到对应快递"));
    }

    @PostMapping("/{id}/pickup")
    @Operation(summary = "为快递生成存放位置和取件码")
    public ResponseEntity<?> createPickupInfo(@PathVariable Long id) {
        // 获取当前管理员及其权限范围
        AdminRoleScope scope = requireCurrentAdminScope();
        
        return service.getById(id)
                .map(parcel -> {
                    // 1. 验证包裹状态
                    if (parcel.getIsSigned() != null && parcel.getIsSigned() == 1) {
                        return ResponseEntity.status(400).body("快递已签收，不能创建取件信息");
                    }
                    if (parcel.getPickupCode() != null && !parcel.getPickupCode().isBlank()) {
                        return ResponseEntity.status(409).body("该快递已经存在取件码");
                    }
                    
                    // 2. 验证站点权限（如果是街道管理员）
                    String currentStation = parcel.getCurrentStation();
                    String adminStation = scope.getStation();
                    
                    if (scope.getRole() == com.campus.station.model.AdminRole.STREET_ADMIN) {
                        if (adminStation != null && currentStation != null) {
                            // 检查管理员是否在包裹当前所在的站点
                            boolean isAtCurrentStation = adminStation.equals(currentStation) || 
                                                       adminStation.contains(currentStation) || 
                                                       currentStation.contains(adminStation);
                            
                            if (!isAtCurrentStation) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您无权对不在本站的包裹进行入库操作（当前包裹在：" + currentStation + "）");
                            }
                        }
                    }
                    
                    // 3. 验证是否为发货站点（发货站点不能入库，必须流转到终点站）
                    // 获取流转记录（按时间正序）
                    java.util.List<ParcelRoute> routes = parcelRouteService.listByTrackingNumber(parcel.getTrackingNumber());
                    if (!routes.isEmpty()) {
                        ParcelRoute firstRoute = routes.get(0);
                        String originStation = firstRoute.getCurrentStation();
                        
                        // 如果当前站点就是发货时的初始站点，且有下一站（说明是物流件而非同城即时达），则禁止入库
                        // 逻辑优化：只要是发货站点，且当前没有流转到其他站点（即 currentStation == originStation），就禁止入库
                        // 除非发货站点就是收货地址（极少见，且通常不走物流逻辑）
                        
                        if (originStation != null && currentStation != null && 
                            (originStation.equals(currentStation) || originStation.contains(currentStation) || currentStation.contains(originStation))) {
                             
                             // 进一步检查：如果这是刚刚创建的包裹（只有一条路由记录），肯定是发货站点
                             // 或者虽然有多条记录，但兜兜转转又回到了发货站点（也不应该直接入库，应该继续发走）
                             
                             // 简单规则：发货站点不能入库。
                             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("发货站点无法进行入库操作，请先流转至下一站");
                        }
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
                    update.setStatus(2);
                    update.setIsSigned(null);
                    Parcel updated = service.update(id, update);
                    
                    // Explicitly sync to warehouse storage
                    stationStorageService.syncFromParcel(updated);
                    
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 生成随机快递单号
     * 格式：YD + 13位数字（时间戳10位 + 随机数3位）
     * 例如：YD1706789012345
     */
    private String generateTrackingNumber() {
        String trackingNumber;
        int attempts = 0;
        do {
            // 使用当前时间戳（秒级）+ 3位随机数
            long timestamp = System.currentTimeMillis() / 1000; // 10位时间戳
            int random = ThreadLocalRandom.current().nextInt(100, 1000); // 3位随机数
            trackingNumber = "YD" + timestamp + random;
            attempts++;
            if (attempts > 10) {
                // 如果尝试10次仍然冲突，使用纯随机数
                long randomNum = ThreadLocalRandom.current().nextLong(1000000000000L, 10000000000000L);
                trackingNumber = "YD" + randomNum;
                break;
            }
        } while (service.getByTrackingNumber(trackingNumber).isPresent());
        return trackingNumber;
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
        requireCurrentAdmin();
        return service.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询所有快递列表")
    public Page<Parcel> list(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Integer status) {
        AdminRoleScope scope = requireCurrentAdminScope();
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return service.listForScopeAndStatus(scope, status, pageable);
        }
        return service.listForScope(scope, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新快递信息")
    public ResponseEntity<Parcel> update(@PathVariable Long id, @RequestBody Parcel req) {
        requireCurrentAdmin();
        Parcel updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除快递")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requireCurrentAdmin();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "批量删除快递")
    public ResponseEntity<Void> deleteBatch(@RequestBody Iterable<Long> ids) {
        requireCurrentAdmin();
        service.deleteBatch(ids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "修改快递状态")
    public ResponseEntity<Parcel> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        requireCurrentAdmin();
        Parcel updated = service.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/debug/{trackingNumber}")
    @Operation(summary = "调试接口：查看包裹完整信息")
    public ResponseEntity<?> debugParcelInfo(@PathVariable String trackingNumber) {
        requireCurrentAdmin();
        
        Parcel parcel = service.getByTrackingNumber(trackingNumber).orElse(null);
        if (parcel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("包裹不存在");
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("parcel", parcel);
        
        // 由于 service 中没有直接获取 route 的方法，这里只能返回 parcel
        // 如果需要 route 信息，建议在 AdminParcelRouteController 中添加或注入 ParcelRouteRepository
        // 但为了简单起见，这里只返回 parcel，这已经足够检查 nextStation 字段了
        
        return ResponseEntity.ok(result);
    }

}
