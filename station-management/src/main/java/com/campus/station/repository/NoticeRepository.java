package com.campus.station.repository;

import com.campus.station.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAllByOrderByCreateTimeDesc(Pageable pageable);
    List<Notice> findByCreatorId(Long creatorId);
}
