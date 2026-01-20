package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.SysAdmin;
import com.campus.station.service.SysAdminService;
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
@RequestMapping("/api/admin")
public class AdminProfileController {

    private final SysAdminService sysAdminService;

    public AdminProfileController(SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    private SysAdmin requireAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }
        return current;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("用户名与密码为必填项");
        }

        return sysAdminService.getByUsername(username)
                .map(admin -> {
                    if (!password.equals(admin.getPassword())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
                    }
                    if (admin.getStatus() != null && admin.getStatus() == (byte) 0) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("账号已禁用");
                    }
                    SessionUtil.setCurrentAdmin(admin);
                    return ResponseEntity.ok(admin);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("管理员不存在"));
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出")
    public ResponseEntity<?> logout() {
        SessionUtil.clearCurrentAdmin();
        return ResponseEntity.ok("登出成功");
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前管理员个人信息（个人中心）")
    public ResponseEntity<?> getProfile() {
        SysAdmin current = requireAdmin();
        return ResponseEntity.ok(current);
    }

    @PutMapping("/profile")
    @Operation(summary = "管理员修改自己的基本信息（用户名、手机号等）")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        SysAdmin current = requireAdmin();

        String username = body.get("username");
        String phone = body.get("phone");

        SysAdmin update = new SysAdmin();
        update.setUsername(username);
        update.setPhone(phone);

        SysAdmin saved;
        try {
            saved = sysAdminService.update(current.getId(), update);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }

        SessionUtil.setCurrentAdmin(saved);

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/profile/password")
    @Operation(summary = "管理员修改自己的密码")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        SysAdmin current = requireAdmin();

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("旧密码和新密码为必填项");
        }
        if (!oldPassword.equals(current.getPassword())) {
            return ResponseEntity.status(400).body("旧密码不正确");
        }

        SysAdmin update = new SysAdmin();
        update.setPassword(newPassword);
        SysAdmin saved = sysAdminService.update(current.getId(), update);

        SessionUtil.setCurrentAdmin(saved);

        return ResponseEntity.ok("密码修改成功");
    }
}
