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

    @PostMapping
    @Operation(summary = "管理员创建用户")
    public ResponseEntity<?> create(@RequestBody SysUser req) {
        requireAdmin();
        if (req.getRole() == null || req.getRole().isBlank()) {
            req.setRole("USER");
        }
        if (req.getStatus() == null) {
            req.setStatus((byte) 1);
        }

        try {
            SysUser created = service.create(req);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "管理员修改用户信息")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysUser req) {
        requireAdmin();
        try {
            SysUser updated = service.update(id, req);
            parcelService.updateReceiverInfo(updated.getId(), updated.getUsername(), updated.getPhone());
            noticeService.updateCreatorNameByUser(updated.getId(), updated.getUsername());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "管理员修改用户状态")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam byte status) {
        requireAdmin();
        SysUser updated = service.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "管理员删除用户")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        requireAdmin();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
