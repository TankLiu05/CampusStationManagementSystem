package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.SysUser;
import com.campus.station.service.NoticeService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "AdminProfile", description = "管理员个人信息相关接口")
@RequestMapping("/api/admin/profile")
public class AdminProfileController {

    private final SysUserService sysUserService;
    private final ParcelService parcelService;
    private final NoticeService noticeService;

    public AdminProfileController(SysUserService sysUserService, ParcelService parcelService, NoticeService noticeService) {
        this.sysUserService = sysUserService;
        this.parcelService = parcelService;
        this.noticeService = noticeService;
    }

    private SysUser requireAdmin() {
        SysUser current = SessionUtil.getCurrentUser();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        if (current.getRole() == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问管理员个人信息接口");
        }
        return current;
    }

    @GetMapping
    @Operation(summary = "获取当前管理员个人信息（个人中心）")
    public ResponseEntity<?> getProfile() {
        SysUser currentUser = requireAdmin();
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping
    @Operation(summary = "管理员修改自己的基本信息（用户名、手机号等）")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        SysUser currentUser = requireAdmin();

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
    @Operation(summary = "管理员修改自己的密码")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        SysUser currentUser = requireAdmin();

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
}
