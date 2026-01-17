package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.SysUser;
import com.campus.station.service.NoticeService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<Page<SysUser>> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        Pageable pageable = PageRequest.of(page, size);
        Page<SysUser> result = service.list(pageable);
        return ResponseEntity.ok(result);
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
}
