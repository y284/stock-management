package com.stock.stock_management.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.stock_management.dto.PaymentDto;
import com.stock.stock_management.entity.Payment;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.SalesOrderRepository;

import com.stock.stock_management.mapper.PaymentMapper;
import com.stock.stock_management.repository.PaymentRepository;
import com.stock.stock_management.service.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    private final SalesOrderRepository salesOrderRepository;

    // ========= Create =========
    @Override
    @Transactional
    public PaymentDto create(PaymentDto dto) {
        precheckCreate(dto);
        Payment entity = mapper.toEntity(dto);
        if (dto.getSalesOrderId() != null) { entity.setSalesOrder(salesOrderRepository.getRef(dto.getSalesOrderId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public PaymentDto update(Long id, PaymentDto dto) {
        // Load current (404 if missing)
        Payment current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("payment not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Payment replaced = mapper.toEntity(dto);

        // Enforce identifier (so Hibernate updates instead of inserting)
        replaced.setId(id);

        // Preserve immutable / audit when omitted in DTO
        if (replaced.getUuid() == null) { replaced.setUuid(current.getUuid()); }
        if (replaced.getCreatedAt() == null) { replaced.setCreatedAt(current.getCreatedAt()); }

        // ===== Version handling =====
        // If DTO carries version -> use it (optimistic locking by Hibernate).
        // Else preserve current version to avoid null-version issues.
        if (replaced.getVersion() == null) {
            replaced.setVersion(current.getVersion());
        }
        if (dto.getSalesOrderId() != null) { replaced.setSalesOrder(salesOrderRepository.getRef(dto.getSalesOrderId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public PaymentDto patch(Long id, PaymentDto dto) {
        Payment entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("payment not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getSalesOrderId() != null) { entity.setSalesOrder(salesOrderRepository.getRef(dto.getSalesOrderId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("payment not found with id=" + id);
        }
        guardDelete(id);
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        repository.findByUuid(uuid).ifPresent(entity -> {
            guardDelete(entity.getId());
            repository.delete(entity);
        });
    }

    // ========= Prechecks derived from schema/spec =========
    private void precheckCreate(PaymentDto dto) {
        if (dto.getAmount() == null) { throw new MissingRequiredFieldException("amount is required"); }
        if (dto.getPaymentMethod() == null) { throw new MissingRequiredFieldException("payment_method is required"); }
        if (dto.getPaymentType() == null) { throw new MissingRequiredFieldException("payment_type is required"); }
        if (dto.getSalesOrderId() != null && !salesOrderRepository.existsById(dto.getSalesOrderId())) { throw new ForeignKeyNotFoundException("sales_order_id references missing sales_order"); }
    }

    private void precheckUpdate(Long id, PaymentDto dto) {
        if (dto.getAmount() == null) { throw new MissingRequiredFieldException("amount is required"); }
        if (dto.getPaymentMethod() == null) { throw new MissingRequiredFieldException("payment_method is required"); }
        if (dto.getPaymentType() == null) { throw new MissingRequiredFieldException("payment_type is required"); }
        if (dto.getSalesOrderId() != null && !salesOrderRepository.existsById(dto.getSalesOrderId())) { throw new ForeignKeyNotFoundException("sales_order_id references missing sales_order"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
