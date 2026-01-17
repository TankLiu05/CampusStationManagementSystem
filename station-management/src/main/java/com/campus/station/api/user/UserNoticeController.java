package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Notice;
import com.campus.station.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UserNotice", description = "用户公告查看接口")
@RequestMapping("/api/user/notice")
public class UserNoticeController {

    private final NoticeService service;

    public UserNoticeController(NoticeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "分页查看公告列表")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        if (!SessionUtil.isLoggedIn()) {
            return ResponseEntity.status(401).body("未登录");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Notice> notices = service.list(pageable);
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看公告详情")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        if (!SessionUtil.isLoggedIn()) {
            return ResponseEntity.status(401).body("未登录");
        }
        return service.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

