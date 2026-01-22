package com.campus.station.service;

import com.campus.station.model.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ReturnRequestService {
    ReturnRequest create(ReturnRequest request);
    Optional<ReturnRequest> getById(Long id);
    Page<ReturnRequest> list(Pageable pageable);
    Page<ReturnRequest> listByUsername(String username, Pageable pageable);
    Page<ReturnRequest> listByStatus(Integer status, Pageable pageable);
    ReturnRequest updateStatus(Long id, Integer status);
}
