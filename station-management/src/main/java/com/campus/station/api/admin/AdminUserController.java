package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.SysUser;
import com.campus.station.service.NoticeService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "AdminUser", description = "管理员用户管理接口")
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final SysUserService service;
    private final ParcelService parcelService;
    private final NoticeService noticeService;

    public AdminUserController(SysUserService service, ParcelService parcelService, NoticeService noticeService) {
        this.service = service;
        this.parcelService = parcelService;
        this.noticeService = noticeService;
    }

    private SysUser requireAdmin() {
        SysUser current = SessionUtil.getCurrentUser();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        if (current.getRole() == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问用户管理接口");
        }
        return current;
    }

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    public ResponseEntity<Page<AdminUserView>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        Pageable pageable = PageRequest.of(page, size);
        Page<SysUser> result = service.list(pageable);
        Page<AdminUserView> views = result.map(AdminUserController::toView);
        return ResponseEntity.ok(views);
    }

    @GetMapping("/byUsername")
    @Operation(summary = "根据用户名查询用户")
    public ResponseEntity<?> getByUsername(@RequestParam String username) {
        requireAdmin();
        return service.getByUsername(username)
                .map(AdminUserController::toView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在"));
    }

    @GetMapping("/byPhone")
    @Operation(summary = "根据手机号查询用户")
    public ResponseEntity<?> getByPhone(@RequestParam String phone) {
        requireAdmin();
        return service.getByPhone(phone)
                .map(AdminUserController::toView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在"));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "管理员修改用户状态")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam byte status) {
        requireAdmin();
        SysUser updated = service.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/password")
    @Operation(summary = "管理员重置用户密码")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        requireAdmin();
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("新密码不能为空");
        }
        service.updatePassword(id, newPassword);
        return ResponseEntity.ok("密码重置成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "管理员删除用户")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        requireAdmin();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static AdminUserView toView(SysUser user) {
        AdminUserView view = new AdminUserView();
        view.setId(user.getId());
        view.setUsername(user.getUsername());
        view.setPhone(user.getPhone());
        view.setEmail(user.getEmail());
        view.setAvatar(user.getAvatar());
        view.setRole(user.getRole());
        view.setStatus(user.getStatus());
        view.setCreateTime(user.getCreateTime());
        view.setUpdateTime(user.getUpdateTime());
        return view;
    }

    public static class AdminUserView {
        private Long id;
        private String username;
        private String phone;
        private String email;
        private String avatar;
        private String role;
        private Byte status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Byte getStatus() {
            return status;
        }

        public void setStatus(Byte status) {
            this.status = status;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }
}
