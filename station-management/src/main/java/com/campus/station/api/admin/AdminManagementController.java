package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.AdminStation;
import com.campus.station.model.SysAdmin;
import com.campus.station.repository.AdminStationRepository;
import com.campus.station.service.AdminRoleScopeService;
import com.campus.station.service.SysAdminService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

@RestController
@Tag(name = "AdminManagement", description = "管理员账号与等级管理接口")
@RequestMapping("/api/admin/management")
public class AdminManagementController {

    private final SysAdminService sysAdminService;
    private final AdminRoleScopeService adminRoleScopeService;
    private final AdminStationRepository adminStationRepository;

    public AdminManagementController(SysAdminService sysAdminService,
                                     AdminRoleScopeService adminRoleScopeService,
                                     AdminStationRepository adminStationRepository) {
        this.sysAdminService = sysAdminService;
        this.adminRoleScopeService = adminRoleScopeService;
        this.adminStationRepository = adminStationRepository;
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

    private boolean canManage(AdminRoleScope currentScope, AdminRole targetRole) {
        AdminRole currentRole = currentScope.getRole();
        if (currentRole == AdminRole.SUPERADMIN) {
            return true;
        }
        if (currentRole == AdminRole.MANAGER) {
            return targetRole == AdminRole.STREET_ADMIN;
        }
        return false;
    }

    private boolean canViewScope(AdminRoleScope currentScope, AdminRoleScope targetScope) {
        if (currentScope == null || targetScope == null) {
            return false;
        }
        AdminRole currentRole = currentScope.getRole();
        AdminRole targetRole = targetScope.getRole();

        if (currentRole == AdminRole.SUPERADMIN) {
            return true;
        }

        // 省级管理员：查看同省的站点管理员
        if (currentRole == AdminRole.MANAGER) {
            String province = currentScope.getProvince();
            if (province == null || province.isBlank()) {
                return false;
            }
            if (targetRole != AdminRole.STREET_ADMIN) {
                return false;
            }
            String targetProvince = targetScope.getProvince();
            if (targetProvince == null) {
                return false;
            }
            return province.equals(targetProvince) || province.startsWith(targetProvince) || targetProvince.startsWith(province);
        }

        // 站点管理员目前没有下级
        return false;
    }

    @PostMapping
    @Operation(summary = "创建管理员")
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, Object> body) {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String phone = (String) body.get("phone");
        String email = (String) body.get("email");
        String roleStr = (String) body.get("role");
        
        // Normalize address fields
        String province = normalizeProvince((String) body.get("province"));
        String city = normalizeCity((String) body.get("city"));
        String station = (String) body.get("station");

        if (username == null || username.isBlank() || password == null || password.isBlank() || roleStr == null) {
            return ResponseEntity.badRequest().body("用户名、密码和角色为必填项");
        }

        AdminRole targetRole;
        try {
            targetRole = AdminRole.valueOf(roleStr);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("角色无效");
        }

        if (targetRole == AdminRole.MANAGER) {
            if (province == null || province.isBlank()) {
                return ResponseEntity.badRequest().body("省级管理员必须填写省份");
            }
        } else if (targetRole == AdminRole.STREET_ADMIN) {
            if (province == null || province.isBlank() || city == null || city.isBlank() || station == null
                    || station.isBlank()) {
                return ResponseEntity.badRequest().body("站点管理员必须填写省份、城市和站点信息");
            }
        }

        String currentProvince = currentScope.getProvince();
        if (currentProvince != null && !currentProvince.isBlank()
                && (targetRole == AdminRole.MANAGER
                        || targetRole == AdminRole.STREET_ADMIN)) {
            if (province == null || (!currentProvince.equals(province) && !province.startsWith(currentProvince) && !currentProvince.startsWith(province))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权跨省创建管理员");
            }
        }

        if (!canManage(currentScope, targetRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权创建该等级管理员");
        }

        SysAdmin sysAdmin = new SysAdmin();
        sysAdmin.setUsername(username);
        sysAdmin.setPassword(password);
        sysAdmin.setPhone(phone);
        sysAdmin.setEmail(email);
        sysAdmin.setStatus((byte) 1);
        sysAdmin.setRole(targetRole);

        SysAdmin createdAdmin;
        try {
            createdAdmin = sysAdminService.create(sysAdmin);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }

        AdminRoleScope scope = new AdminRoleScope();
        scope.setAdminId(createdAdmin.getId());
        scope.setParentAdminId(currentScope.getAdminId());
        scope.setRole(targetRole);
        scope.setProvince(province);
        scope.setCity(city);
        scope.setStation(station);

        AdminRoleScope createdScope = adminRoleScopeService.create(scope);

        if (targetRole == AdminRole.STREET_ADMIN
                && province != null && !province.isBlank()
                && city != null && !city.isBlank()
                && station != null && !station.isBlank()) {
            boolean exists = adminStationRepository.existsByProvinceAndCityAndStation(province, city, station);
            if (!exists) {
                AdminStation adminStation = new AdminStation();
                adminStation.setProvince(province);
                adminStation.setCity(city);
                adminStation.setStation(station);
                if (phone != null) {
                    adminStation.setPhone(phone);
                } else {
                    adminStation.setPhone("");
                }
                adminStation.setUsername(username);
                try {
                    adminStationRepository.save(adminStation);
                } catch (Exception e) {
                }
            }
        }

        return ResponseEntity.ok(new AdminDetailView(createdAdmin, createdScope));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前管理员详情")
    public ResponseEntity<?> getCurrentAdminDetail() {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        SysAdmin admin = currentScope.getAdmin();
        if (admin == null) {
            Optional<SysAdmin> adminOpt = sysAdminService.getById(currentScope.getAdminId());
            if (!adminOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员不存在");
            }
            admin = adminOpt.get();
        }

        return ResponseEntity.ok(new AdminDetailView(admin, currentScope));
    }

    @GetMapping("/stations/all")
    @Operation(summary = "获取所有站点信息")
    public ResponseEntity<List<AdminStation>> getAllStations() {
        requireCurrentAdmin();
        return ResponseEntity.ok(adminStationRepository.findAll());
    }

    @GetMapping
    @Operation(summary = "查询当前管理员可管理的管理员列表")
    public ResponseEntity<?> listManageableAdmins() {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        if (currentScope.getRole() == AdminRole.SUPERADMIN) {
            List<AdminRoleScope> allScopes = adminRoleScopeService.listAll();
            return ResponseEntity.ok(allScopes);
        }

        List<AdminRoleScope> allScopes = adminRoleScopeService.listAll();
        List<AdminRoleScope> visibleScopes = allScopes.stream()
                .filter(scope -> !scope.getAdminId().equals(currentScope.getAdminId()))
                .filter(scope -> canViewScope(currentScope, scope))
                .collect(Collectors.toList());
        return ResponseEntity.ok(visibleScopes);
    }

    @GetMapping("/{adminId}")
    @Operation(summary = "获取指定管理员详情")
    public ResponseEntity<?> getAdmin(@PathVariable Long adminId) {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        Optional<SysAdmin> adminOpt = sysAdminService.getById(adminId);
        if (!adminOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员不存在");
        }
        SysAdmin admin = adminOpt.get();
        Optional<AdminRoleScope> scopeOpt = adminRoleScopeService.getByAdminId(admin.getId());
        if (!scopeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员角色范围不存在");
        }
        AdminRoleScope scope = scopeOpt.get();
        if (!canManage(currentScope, scope.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权查看该管理员");
        }
        return ResponseEntity.ok(new AdminDetailView(admin, scope));
    }

    @PutMapping("/{adminId}")
    @Operation(summary = "更新管理员信息和范围")
    public ResponseEntity<?> updateAdmin(@PathVariable Long adminId, @RequestBody Map<String, Object> body) {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        Optional<SysAdmin> adminOpt = sysAdminService.getById(adminId);
        if (!adminOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员不存在");
        }
        SysAdmin admin = adminOpt.get();
        Optional<AdminRoleScope> scopeOpt = adminRoleScopeService.getByAdminId(admin.getId());
        if (!scopeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员角色范围不存在");
        }
        AdminRoleScope scope = scopeOpt.get();

        if (!canManage(currentScope, scope.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权修改该管理员");
        }

        String phone = (String) body.get("phone");
        String email = (String) body.get("email");
        // Normalize address fields
        String province = normalizeProvince((String) body.get("province"));
        String city = normalizeCity((String) body.get("city"));
        String station = (String) body.get("station");

        if (phone != null) {
            admin.setPhone(phone);
        }
        if (email != null) {
            admin.setEmail(email);
        }

        SysAdmin savedAdmin = sysAdminService.update(adminId, admin);

        AdminRoleScope updateScope = new AdminRoleScope();
        updateScope.setProvince(province);
        updateScope.setCity(city);
        updateScope.setStation(station);
        AdminRoleScope savedScope = adminRoleScopeService.update(scope.getId(), updateScope);

        if (savedScope.getRole() == AdminRole.STREET_ADMIN
                && savedScope.getProvince() != null && !savedScope.getProvince().isBlank()
                && savedScope.getCity() != null && !savedScope.getCity().isBlank()
                && savedScope.getStation() != null && !savedScope.getStation().isBlank()) {
            Optional<AdminStation> stationOpt = adminStationRepository.findByProvinceAndCityAndStation(
                    savedScope.getProvince(), savedScope.getCity(), savedScope.getStation());
            AdminStation adminStation;
            if (stationOpt.isPresent()) {
                adminStation = stationOpt.get();
            } else {
                adminStation = new AdminStation();
                adminStation.setProvince(savedScope.getProvince());
                adminStation.setCity(savedScope.getCity());
                adminStation.setStation(savedScope.getStation());
            }
            adminStation.setUsername(savedAdmin.getUsername());
            if (savedAdmin.getPhone() != null) {
                adminStation.setPhone(savedAdmin.getPhone());
            } else {
                adminStation.setPhone("");
            }
            try {
                adminStationRepository.save(adminStation);
            } catch (Exception e) {
            }
        }

        return ResponseEntity.ok(new AdminDetailView(savedAdmin, savedScope));
    }

    @DeleteMapping("/{adminId}")
    @Operation(summary = "删除管理员")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long adminId) {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        Optional<SysAdmin> adminOpt = sysAdminService.getById(adminId);
        if (!adminOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员不存在");
        }
        SysAdmin admin = adminOpt.get();

        Optional<AdminRoleScope> scopeOpt = adminRoleScopeService.getByAdminId(admin.getId());
        if (!scopeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员角色范围不存在");
        }
        AdminRoleScope scope = scopeOpt.get();

        if (!canManage(currentScope, scope.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权删除该管理员");
        }

        adminRoleScopeService.delete(scope.getId());
        sysAdminService.delete(adminId);

        return ResponseEntity.noContent().build();
    }

    private String normalizeProvince(String province) {
        if (province == null || province.isBlank()) {
            return province;
        }
        province = province.trim();
        if (province.endsWith("省") || province.endsWith("市") || province.endsWith("区")) {
            return province;
        }
        
        // Direct controlled municipalities
        if (province.equals("北京") || province.equals("天津") || province.equals("上海") || province.equals("重庆")) {
            return province + "市";
        }
        
        // Autonomous regions and SARs
        if (java.util.Arrays.asList("内蒙古", "广西", "西藏", "宁夏", "新疆", "香港", "澳门").contains(province)) {
             return province;
        }
        
        // Default: append "省"
        return province + "省";
    }

    private String normalizeCity(String city) {
        if (city == null || city.isBlank()) {
            return city;
        }
        city = city.trim();
        if (city.endsWith("市") || city.endsWith("州") || city.endsWith("盟") || city.endsWith("地区") || city.endsWith("区") || city.endsWith("县")) {
            return city;
        }
        return city + "市";
    }

    static class AdminDetailView {
        private Long id;
        private String username;
        private String phone;
        private String email;
        private Byte status;
        private AdminRole role;
        private String province;
        private String city;
        private String station;

        AdminDetailView(SysAdmin admin, AdminRoleScope scope) {
            this.id = admin.getId();
            this.username = admin.getUsername();
            this.phone = admin.getPhone();
            this.email = admin.getEmail();
            this.status = admin.getStatus();
            this.role = scope.getRole();
            this.province = scope.getProvince();
            this.city = scope.getCity();
            this.station = scope.getStation();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Byte getStatus() {
            return status;
        }

        public void setStatus(Byte status) {
            this.status = status;
        }

        public AdminRole getRole() {
            return role;
        }

        public void setRole(AdminRole role) {
            this.role = role;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStation() {
            return station;
        }

        public void setStation(String station) {
            this.station = station;
        }
    }
}
