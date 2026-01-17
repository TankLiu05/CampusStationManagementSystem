package com.campus.station.service.impl;

import com.campus.station.model.Notice;
import com.campus.station.repository.NoticeRepository;
import com.campus.station.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository repository;

    public NoticeServiceImpl(NoticeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Notice create(Notice notice) {
        return repository.save(notice);
    }

    @Override
    @Transactional
    public Notice update(Long id, Notice update) {
        Notice existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        if (update.getTitle() != null) {
            existing.setTitle(update.getTitle());
        }
        if (update.getContent() != null) {
            existing.setContent(update.getContent());
        }
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Notice> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<Notice> list(Pageable pageable) {
        return repository.findAllByOrderByCreateTimeDesc(pageable);
    }
}

