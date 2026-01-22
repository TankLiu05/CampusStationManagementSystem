package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.ReturnRequest;
import com.campus.station.model.SysUser;
import com.campus.station.service.ReturnRequestService;
import com.campus.station.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "UserReturnRequest", description = "用户退货申请接口")
@RequestMapping("/api/user/return-request")
public class UserReturnRequestController {

    private final ReturnRequestService service;
    private final SysUserService sysUserService;

    public UserReturnRequestController(ReturnRequestService service, SysUserService sysUserService) {
        this.service = service;
        this.sysUserService = sysUserService;
    }

    @PostMapping
    @Operation(summary = "提交退货申请")
    public ResponseEntity<?> create(@RequestBody ReturnRequest req) {
        Long userId = SessionUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        // Auto-fill username/phone if not provided or just use current user's info
        // The user said "username, phone, tracking number, reason" in the form.
        // We should validate these.
        
        if (req.getTrackingNumber() == null || req.getTrackingNumber().isBlank()) {
            return ResponseEntity.badRequest().body("快递单号不能为空");
        }
        if (req.getReason() == null || req.getReason().isBlank()) {
            return ResponseEntity.badRequest().body("退货原因不能为空");
        }
        
        // If username/phone not provided, use current user's
        if (req.getUsername() == null || req.getUsername().isBlank()) {
             SysUser user = sysUserService.getById(userId).orElse(null);
             if (user != null) {
                 req.setUsername(user.getUsername());
                 req.setPhone(user.getPhone());
             }
        }
        
        if (req.getUsername() == null || req.getUsername().isBlank()) {
             return ResponseEntity.badRequest().body("用户名不能为空");
        }
        if (req.getPhone() == null || req.getPhone().isBlank()) {
             return ResponseEntity.badRequest().body("联系电话不能为空");
        }

        req.setStatus(0); // Default pending
        ReturnRequest created = service.create(req);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "查询我的退货申请")
    public ResponseEntity<?> listMyRequests(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = SessionUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        // Since we don't store userId in ReturnRequest (only username), we rely on username query.
        // It might be better to store userId, but user requirement was username, phone...
        // We'll query by username of current user.
        
        SysUser user = sysUserService.getById(userId).orElse(null);
        if (user == null) {
             return ResponseEntity.status(401).body("用户不存在");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ReturnRequest> requests = service.listByUsername(user.getUsername(), pageable);
        return ResponseEntity.ok(requests);
    }
}
