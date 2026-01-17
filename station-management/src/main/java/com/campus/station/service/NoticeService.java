package com.campus.station.service;

import com.campus.station.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeService {
    Notice create(Notice notice);
    Notice update(Long id, Notice update);
    void delete(Long id);
    Optional<Notice> getById(Long id);
    Page<Notice> list(Pageable pageable);
    void updateCreatorNameByUser(Long creatorId, String creatorName);
}
