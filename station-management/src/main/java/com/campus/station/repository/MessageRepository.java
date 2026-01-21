package com.campus.station.repository;

import com.campus.station.model.Message;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 根据用户ID查询留言列表
     */
    List<Message> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据状态查询留言
     */
    Page<Message> findByStatus(Integer status, Pageable pageable);
}
