package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Message;
import com.campus.station.model.SysAdmin;
import com.campus.station.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "AdminMessage", description = "管理员留言管理接口")
@RequestMapping("/api/admin/message")
public class AdminMessageController {

    private final MessageService messageService;

    public AdminMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    private SysAdmin requireAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }
        return current;
    }

    @GetMapping
    @Operation(summary = "分页查询留言列表")
    public ResponseEntity<Page<Message>> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Integer status) {
        requireAdmin();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        if (status != null) {
            return ResponseEntity.ok(messageService.listByStatus(status, pageable));
        }
        return ResponseEntity.ok(messageService.list(pageable));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "回复留言")
    public ResponseEntity<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin();
        String replyContent = body.get("replyContent");
        if (replyContent == null || replyContent.isBlank()) {
            return ResponseEntity.badRequest().body("回复内容不能为空");
        }
        
        Message message = new Message();
        message.setReplyContent(replyContent);
        
        try {
            Message updated = messageService.update(id, message);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除留言")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        requireAdmin();
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
