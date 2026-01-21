package com.campus.station.service.impl;

import com.campus.station.model.Message;
import com.campus.station.repository.MessageRepository;
import com.campus.station.service.MessageService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;

    public MessageServiceImpl(MessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Message create(Message message) {
        return repository.save(message);
    }

    @Override
    @Transactional
    public Message update(Long id, Message message) {
        return repository.findById(id)
                .map(existing -> {
                    if (message.getReplyContent() != null) {
                        existing.setReplyContent(message.getReplyContent());
                        existing.setStatus(1); // 自动标记为已回复
                    }
                    if (message.getStatus() != null) {
                        existing.setStatus(message.getStatus());
                    }
                    // 用户不能修改内容，只能管理员回复，或者只能追加？暂时只允许改回复和状态
                    return repository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Message> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<Message> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public List<Message> listByUserId(Long userId) {
        return repository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public Page<Message> listByStatus(Integer status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }
}
