package com.campus.station.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.SysUser;
import com.campus.station.service.SysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "SysUser", description = "用户与管理员相关接口")
@RequestMapping("/api/sysUser")
public class SysUserController {

    private final SysUserService service;

    public SysUserController(SysUserService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("用户名与密码为必填项");
        }

        return service.getByUsername(username)
                .map(user -> {
                    if (!password.equals(user.getPassword())) {
                        return ResponseEntity.status(401).body("用户名或密码错误");
                    }
                    if (user.getStatus() != null && user.getStatus() == (byte)0) {
                        return ResponseEntity.status(403).body("账号已禁用");
                    }
                    // 登录成功，将用户信息存入 Session
                    SessionUtil.setCurrentUser(user);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("用户不存在"));
    }

    @PostMapping
    @Operation(summary = "普通用户注册")
    public ResponseEntity<?> create(@RequestBody SysUser req) {
        // 仅允许注册普通用户
        if (req.getRole() != null && !"USER".equalsIgnoreCase(req.getRole())) {
            return ResponseEntity.status(403).body("仅允许注册普通用户");
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

    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息")
    public ResponseEntity<?> getCurrentUser() {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ResponseEntity<?> logout() {
        SessionUtil.clearCurrentUser();
        return ResponseEntity.ok("登出成功");
    }
}
