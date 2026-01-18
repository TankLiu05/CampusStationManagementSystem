package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Notice;
import com.campus.station.model.SysUser;
import com.campus.station.service.NoticeService;
import com.campus.station.service.SysAdminService;
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
@Tag(name = "AdminNotice", description = "管理员公告管理接口")
@RequestMapping("/api/admin/notice")
public class AdminNoticeController {

    private final NoticeService service;
    private final SysAdminService sysAdminService;

    public AdminNoticeController(NoticeService service, SysAdminService sysAdminService) {
        this.service = service;
        this.sysAdminService = sysAdminService;
    }

    private SysUser requireAdmin() {
        SysUser current = SessionUtil.getCurrentUser();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        if (sysAdminService.getByUserId(current.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问公告管理接口");
        }
        return current;
    }

    @PostMapping
    @Operation(summary = "创建公告")
    public ResponseEntity<Notice> create(@RequestBody Notice req) {
        SysUser admin = requireAdmin();
        req.setCreatorId(admin.getId());
        req.setCreatorName(admin.getUsername());
        Notice created = service.create(req);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公告")
    public ResponseEntity<Notice> update(@PathVariable Long id, @RequestBody Notice req) {
        requireAdmin();
        Notice updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requireAdmin();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "分页查询公告列表（管理员查看）")
    public ResponseEntity<Page<Notice>> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看公告详情（管理员查看）")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        requireAdmin();
        return service.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
