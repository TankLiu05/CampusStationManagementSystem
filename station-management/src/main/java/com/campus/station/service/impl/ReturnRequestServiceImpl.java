package com.campus.station.service.impl;

import com.campus.station.model.ReturnRequest;
import com.campus.station.repository.ReturnRequestRepository;
import com.campus.station.service.ReturnRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository repository;

    public ReturnRequestServiceImpl(ReturnRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ReturnRequest create(ReturnRequest request) {
        return repository.save(request);
    }

    @Override
    public Optional<ReturnRequest> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<ReturnRequest> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<ReturnRequest> listByUsername(String username, Pageable pageable) {
        return repository.findByUsername(username, pageable);
    }

    @Override
    public Page<ReturnRequest> listByStatus(Integer status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public ReturnRequest updateStatus(Long id, Integer status) {
        ReturnRequest request = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("退货申请不存在"));
        request.setStatus(status);
        return repository.save(request);
    }
}
