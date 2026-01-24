package com.campus.station.api.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRole;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.SysAdmin;
import com.campus.station.model.SysUser;
import com.campus.station.repository.AdminRoleScopeRepository;
import com.campus.station.repository.SysAdminRepository;
import com.campus.station.service.NoticeService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "UserProfile", description = "用户个人信息相关接口")
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final SysUserService sysUserService;
    private final ParcelService parcelService;
    private final NoticeService noticeService;
    private final SysAdminRepository sysAdminRepository;
    private final AdminRoleScopeRepository adminRoleScopeRepository;

    public UserProfileController(SysUserService sysUserService, ParcelService parcelService, NoticeService noticeService, SysAdminRepository sysAdminRepository, AdminRoleScopeRepository adminRoleScopeRepository) {
        this.sysUserService = sysUserService;
        this.parcelService = parcelService;
        this.noticeService = noticeService;
        this.sysAdminRepository = sysAdminRepository;
        this.adminRoleScopeRepository = adminRoleScopeRepository;
    }

    @GetMapping
    @Operation(summary = "获取当前用户个人信息（个人中心）")
    public ResponseEntity<?> getProfile() {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping
    @Operation(summary = "修改当前用户基本信息（用户名、手机号等）")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        String username = body.get("username");
        String phone = body.get("phone");

        SysUser update = new SysUser();
        update.setUsername(username);
        update.setPhone(phone);

        SysUser saved;
        try {
            saved = sysUserService.update(currentUser.getId(), update);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }

        parcelService.updateReceiverInfo(saved.getId(), saved.getUsername(), saved.getPhone());
        noticeService.updateCreatorNameByUser(saved.getId(), saved.getUsername());

        SessionUtil.setCurrentUser(saved);

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/password")
    @Operation(summary = "修改当前用户密码")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("旧密码和新密码为必填项");
        }
        if (!oldPassword.equals(currentUser.getPassword())) {
            return ResponseEntity.status(400).body("旧密码不正确");
        }

        SysUser update = new SysUser();
        update.setPassword(newPassword);
        SysUser saved = sysUserService.update(currentUser.getId(), update);

        SessionUtil.setCurrentUser(saved);

        return ResponseEntity.ok("密码修改成功");
    }

    @PostMapping("/upgrade-to-admin")
    @Operation(summary = "用户升级为超级管理员")
    public ResponseEntity<?> upgradeToAdmin(@RequestBody Map<String, String> body) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        String adminPassword = body.get("adminPassword");
        if (adminPassword == null || adminPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("请输入超级管理员密码");
        }

        // 验证超级管理员密码
        if (!"admin123".equals(adminPassword)) {
            return ResponseEntity.status(403).body("超级管理员密码错误");
        }

        // 检查是否已经是管理员
        if (sysAdminRepository.existsByUsername(currentUser.getUsername())) {
            return ResponseEntity.status(400).body("该用户已经是管理员");
        }

        // 创建超级管理员账号
        SysAdmin admin = new SysAdmin();
        admin.setUsername(currentUser.getUsername());
        admin.setPassword(currentUser.getPassword());
        admin.setPhone(currentUser.getPhone());
        admin.setEmail(currentUser.getEmail());
        admin.setRole(AdminRole.SUPERADMIN);
        admin.setStatus((byte) 1); // 激活状态
        
        SysAdmin savedAdmin = sysAdminRepository.save(admin);

        // 创建管理员角色权限范围
        AdminRoleScope roleScope = new AdminRoleScope();
        roleScope.setAdminId(savedAdmin.getId());
        roleScope.setParentAdminId(null); // 超级管理员没有父级
        roleScope.setRole(AdminRole.SUPERADMIN);
        roleScope.setProvince(null); // 超级管理员没有地域限制
        roleScope.setCity(null);
        roleScope.setStation(null);
        
        adminRoleScopeRepository.save(roleScope);

        // 更新用户表中的角色
        SysUser update = new SysUser();
        update.setRole(AdminRole.SUPERADMIN);
        SysUser saved = sysUserService.update(currentUser.getId(), update);
        SessionUtil.setCurrentUser(saved);

        return ResponseEntity.ok("升级为超级管理员成功，请重新登录");
    }
}
