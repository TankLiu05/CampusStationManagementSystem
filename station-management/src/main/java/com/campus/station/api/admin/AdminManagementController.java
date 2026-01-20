package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.SysAdmin;
import com.campus.station.model.SysUser;
import com.campus.station.service.AdminRoleScopeService;
import com.campus.station.service.SysAdminService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private final SysUserService sysUserService;
    private final SysAdminService sysAdminService;
    private final AdminRoleScopeService adminRoleScopeService;

    public AdminManagementController(SysUserService sysUserService,
                                     SysAdminService sysAdminService,
                                     AdminRoleScopeService adminRoleScopeService) {
        this.sysUserService = sysUserService;
        this.sysAdminService = sysAdminService;
        this.adminRoleScopeService = adminRoleScopeService;
    }

    private SysUser requireLoginUser() {
        SysUser current = SessionUtil.getCurrentUser();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return current;
    }

    private SysAdmin requireCurrentAdmin() {
        SysUser current = requireLoginUser();
        return sysAdminService.getByUserId(current.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "非管理员，无权操作"));
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
            return targetRole == AdminRole.CITY_ADMIN || targetRole == AdminRole.STREET_ADMIN;
        }
        if (currentRole == AdminRole.CITY_ADMIN) {
            return targetRole == AdminRole.STREET_ADMIN;
        }
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
        String province = (String) body.get("province");
        String city = (String) body.get("city");
        Long stationId = parseStationId(body.get("stationId"));
        if (body.get("stationId") != null && stationId == null) {
            return ResponseEntity.badRequest().body("站点ID必须为数字");
        }

        if (username == null || username.isBlank() || password == null || password.isBlank() || roleStr == null) {
            return ResponseEntity.badRequest().body("用户名、密码和角色为必填项");
        }

        AdminRole targetRole;
        try {
            targetRole = AdminRole.valueOf(roleStr);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("角色无效");
        }

        if (!canManage(currentScope, targetRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权创建该等级管理员");
        }

        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(password);
        sysUser.setPhone(phone);
        sysUser.setEmail(email);
        sysUser.setStatus((byte) 1);

        SysUser createdUser;
        try {
            createdUser = sysUserService.create(sysUser);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }

        SysAdmin sysAdmin = new SysAdmin();
        sysAdmin.setUserId(createdUser.getId());
        sysAdmin.setUsername(username);
        sysAdmin.setPassword(password);
        sysAdmin.setPhone(phone);
        sysAdmin.setEmail(email);
        sysAdmin.setStatus((byte) 1);

        SysAdmin createdAdmin = sysAdminService.create(sysAdmin);

        AdminRoleScope scope = new AdminRoleScope();
        scope.setAdminId(createdAdmin.getId());
        scope.setParentAdminId(currentScope.getAdminId());
        scope.setRole(targetRole);
        scope.setProvince(province);
        scope.setCity(city);
        scope.setStationId(stationId);

        AdminRoleScope createdScope = adminRoleScopeService.create(scope);

        return ResponseEntity.ok(new AdminDetailView(createdAdmin, createdScope));
    }

    @GetMapping
    @Operation(summary = "查询当前管理员可管理的管理员列表")
    public ResponseEntity<?> listManageableAdmins() {
        AdminRoleScope currentScope = requireCurrentAdminScope();

        if (currentScope.getRole() == AdminRole.SUPERADMIN) {
            List<AdminRoleScope> allScopes = adminRoleScopeService.getByParentAdminId(null);
            return ResponseEntity.ok(allScopes);
        }

        List<AdminRoleScope> scopes = adminRoleScopeService.getByParentAdminId(currentScope.getAdminId());
        return ResponseEntity.ok(scopes);
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
        String province = (String) body.get("province");
        String city = (String) body.get("city");
        Long stationId = parseStationId(body.get("stationId"));
        if (body.get("stationId") != null && stationId == null) {
            return ResponseEntity.badRequest().body("站点ID必须为数字");
        }

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
        updateScope.setStationId(stationId);
        AdminRoleScope savedScope = adminRoleScopeService.update(scope.getId(), updateScope);

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

    private Long parseStationId(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
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
        private Long stationId;

        AdminDetailView(SysAdmin admin, AdminRoleScope scope) {
            this.id = admin.getId();
            this.username = admin.getUsername();
            this.phone = admin.getPhone();
            this.email = admin.getEmail();
            this.status = admin.getStatus();
            this.role = scope.getRole();
            this.province = scope.getProvince();
            this.city = scope.getCity();
            this.stationId = scope.getStationId();
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

        public Long getStationId() {
            return stationId;
        }

        public void setStationId(Long stationId) {
            this.stationId = stationId;
        }
    }
}
