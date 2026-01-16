package com.campus.station.api;

import com.campus.station.model.SysUser;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@Tag(name = "SysUser", description = "用户与管理员相关接口")
@RequestMapping("/api/sysUser")
public class SysUserController {

    private final SysUserService service;

    public SysUserController(SysUserService service) {
        this.service = service;
    }

    @PostMapping("/admin")
    @Operation(summary = "创建管理员用户")
    public ResponseEntity<?> createAdmin(@RequestBody SysUser req) {
        // 管理员创建接口：强制角色为 ADMIN
        req.setRole("ADMIN");
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

    @PostMapping("/loginByPhone")
    @Operation(summary = "手机号登录")
    public ResponseEntity<?> loginByPhone(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        if (phone == null || password == null) {
            return ResponseEntity.badRequest().body("手机号与密码为必填项");
        }

        return service.getByPhone(phone)
                .map(user -> {
                    if (!password.equals(user.getPassword())) {
                        return ResponseEntity.status(401).body("手机号或密码错误");
                    }
                    if (user.getStatus() != null && user.getStatus() == (byte)0) {
                        return ResponseEntity.status(403).body("账号已禁用");
                    }
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("用户不存在"));
    }

    @PostMapping
    @Operation(summary = "创建普通用户")
    public ResponseEntity<?> create(@RequestBody SysUser req) {
        // 仅允许创建普通用户
        if (req.getRole() != null && !"USER".equalsIgnoreCase(req.getRole())) {
            return ResponseEntity.status(403).body("仅允许创建普通用户");
        }
        req.setRole("USER");
        if (req.getStatus() == null) {
            req.setStatus((byte) 1);
        }

        // 若缺少用户名但提供了手机号，则用手机号作为用户名
        if ((req.getUsername() == null || req.getUsername().isBlank()) && req.getPhone() != null && !req.getPhone().isBlank()) {
            req.setUsername(req.getPhone());
        }

        try {
            SysUser created = service.create(req);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "按 ID 查询用户")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return service.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "按 ID 更新用户信息")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysUser req) {
        try {
            SysUser updated = service.update(id, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if ("用户名已存在".equals(msg)) {
                return ResponseEntity.status(409).body(msg);
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "按 ID 删除用户")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    public ResponseEntity<Page<SysUser>> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> result = service.list(PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改用户状态")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam byte status) {
        try {
            SysUser updated = service.changeStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
