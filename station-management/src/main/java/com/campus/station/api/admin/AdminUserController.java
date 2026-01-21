package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.SysAdmin;
import com.campus.station.model.SysUser;
import com.campus.station.model.Location;
import com.campus.station.service.NoticeService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysUserService;
import com.campus.station.service.LocationService;
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
    private final LocationService locationService;

    public AdminUserController(SysUserService service,
                               ParcelService parcelService,
                               NoticeService noticeService,
                               LocationService locationService) {
        this.service = service;
        this.parcelService = parcelService;
        this.noticeService = noticeService;
        this.locationService = locationService;
    }

    private SysAdmin requireAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
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
        java.util.Optional<SysUser> optional = service.getByUsername(username);
        if (!optional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }
        AdminUserView view = toView(optional.get());
        return ResponseEntity.ok(view);
    }

    @GetMapping("/byPhone")
    @Operation(summary = "根据手机号查询用户")
    public ResponseEntity<?> getByPhone(@RequestParam String phone) {
        requireAdmin();
        java.util.Optional<SysUser> optional = service.getByPhone(phone);
        if (!optional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }
        AdminUserView view = toView(optional.get());
        return ResponseEntity.ok(view);
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

    @GetMapping("/{id}/location/default")
    @Operation(summary = "管理员查询用户默认收货地址")
    public ResponseEntity<?> getUserDefaultLocation(@PathVariable Long id) {
        requireAdmin();
        return locationService.getDefaultByUserId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("该用户未设置默认收货地址"));
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
