package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Message;
import com.campus.station.model.SysUser;
import com.campus.station.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UserMessage", description = "用户留言相关接口")
@RequestMapping("/api/user/message")
public class UserMessageController {

    private final MessageService messageService;

    public UserMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @Operation(summary = "提交留言")
    public ResponseEntity<?> create(@RequestBody Message req) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        if (req.getContent() == null || req.getContent().isBlank()) {
            return ResponseEntity.badRequest().body("留言内容不能为空");
        }

        Message message = new Message();
        message.setUserId(currentUser.getId());
        message.setUsername(currentUser.getUsername());
        message.setPhone(currentUser.getPhone());
        message.setContent(req.getContent());
        
        // 初始状态
        message.setStatus(0);
        message.setReplyContent(null);

        Message created = messageService.create(message);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "获取我的留言列表")
    public ResponseEntity<?> listMyMessages() {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        List<Message> messages = messageService.listByUserId(currentUser.getId());
        return ResponseEntity.ok(messages);
    }
}
