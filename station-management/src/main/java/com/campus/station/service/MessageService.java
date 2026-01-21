package com.campus.station.service;

import com.campus.station.model.Message;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    
    /**
     * 创建留言
     */
    Message create(Message message);

    /**
     * 更新留言（用于管理员回复）
     */
    Message update(Long id, Message message);

    /**
     * 删除留言
     */
    void delete(Long id);

    /**
     * 根据ID获取留言
     */
    Optional<Message> getById(Long id);

    /**
     * 获取所有留言（分页）
     */
    Page<Message> list(Pageable pageable);

    /**
     * 根据用户ID获取留言列表
     */
    List<Message> listByUserId(Long userId);

    /**
     * 搜索留言（根据状态）
     */
    Page<Message> listByStatus(Integer status, Pageable pageable);
}
