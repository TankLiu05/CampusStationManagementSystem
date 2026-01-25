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

    @GetMapping
    @Operation(summary = "获取快递列表")
    public ResponseEntity<Page<Parcel>> list(Pageable pageable) {
        AdminRoleScope scope = requireCurrentAdminScope();
        return ResponseEntity.ok(service.listForScope(scope, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取快递详情")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        AdminRoleScope scope = requireCurrentAdminScope();
        return service.getById(id)
                .filter(parcel -> service.isParcelVisibleForScope(scope, parcel))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新快递信息")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Parcel parcel) {
        AdminRoleScope scope = requireCurrentAdminScope();
        return service.getById(id)
                .filter(p -> service.isParcelVisibleForScope(scope, p))
                .map(p -> ResponseEntity.ok(service.update(id, parcel)))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除快递")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        requireCurrentAdmin();
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "批量删除快递")
    public ResponseEntity<?> deleteBatch(@RequestBody java.util.List<Long> ids) {
        requireCurrentAdmin();
        service.deleteBatch(ids);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "修改快递状态")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        requireCurrentAdmin();
        return ResponseEntity.ok(service.changeStatus(id, status));
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
                    String adminStation = scope.getStation();
                    
                    // 3. 验证入库权限：只有物流信息的最新“下一站”才有权入库
                    // 获取流转记录
                    java.util.List<ParcelRoute> routes = parcelRouteService.listByTrackingNumber(parcel.getTrackingNumber());
                    
                    if (routes.isEmpty()) {
                        // 如果没有任何流转记录，说明还在发货地且未发出，禁止入库
                         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("包裹尚未开始流转，无法进行入库操作");
                    }
                    
                    // 获取最新的一条流转记录
                    // 假设 listByTrackingNumber 返回的列表可能未排序，手动按 ID 或 创建时间 倒序排序
                    routes.sort((a, b) -> {
                        if (b.getCreateTime() != null && a.getCreateTime() != null) {
                            return b.getCreateTime().compareTo(a.getCreateTime());
                        }
                        return b.getId().compareTo(a.getId());
                    });
                    
                    ParcelRoute latestRoute = routes.get(0);
                    String authorizedStation = latestRoute.getNextStation();
                    
                    if (authorizedStation == null || authorizedStation.isBlank()) {
                         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无法确定下一站信息，禁止入库");
                    }
                    
                    // 检查当前管理员是否属于该授权站点
                    boolean isAuthorized = false;
                    if (adminStation != null) {
                        if (adminStation.equals(authorizedStation) || 
                            adminStation.contains(authorizedStation) || 
                            authorizedStation.contains(adminStation)) {
                            isAuthorized = true;
                        }
                    }
                    
                    if (!isAuthorized) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只有物流下一站（" + authorizedStation + "）才有权入库，您当前为：" + (adminStation != null ? adminStation : "未知"));
                    }

                    String pickupCode;
                    do {
                        pickupCode = PickupCodeUtil.generate();
                    } while (service.findActiveByPickupCode(pickupCode).isPresent());
                    String location;
                    do {
                        location = generateRandomLocation();
                    } while (service.findActiveByLocationAndStation(location, authorizedStation).isPresent());

                    Parcel update = new Parcel();
                    update.setLocation(location);
                    update.setPickupCode(pickupCode);
                    update.setStatus(2);
                    update.setIsSigned(null);
                    // Update current station to the station performing the inbound
                    update.setCurrentStation(authorizedStation);
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
        int area = ThreadLocalRandom.current().nextInt(1, 5); // A-D area (1-4)
        int shelf = ThreadLocalRandom.current().nextInt(1, 11); // 1-10 shelf
        int position = ThreadLocalRandom.current().nextInt(1000, 10000); // 1000-9999
        char areaChar = (char) ('A' + area - 1);
        return String.format("%c区-%d号货架-%d", areaChar, shelf, position);
    }
}
