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

import com.stock.stock_management.dto.PurchaseInvoiceDto;
import com.stock.stock_management.entity.PurchaseInvoice;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.PurchaseOrderRepository;

import com.stock.stock_management.mapper.PurchaseInvoiceMapper;
import com.stock.stock_management.repository.PurchaseInvoiceRepository;
import com.stock.stock_management.service.PurchaseInvoiceService;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceServiceImpl implements PurchaseInvoiceService {

    private final PurchaseInvoiceRepository repository;
    private final PurchaseInvoiceMapper mapper;

    private final PurchaseOrderRepository purchaseOrderRepository;

    // ========= Create =========
    @Override
    @Transactional
    public PurchaseInvoiceDto create(PurchaseInvoiceDto dto) {
        precheckCreate(dto);
        PurchaseInvoice entity = mapper.toEntity(dto);
        if (dto.getPurchaseOrderId() != null) { entity.setPurchaseOrder(purchaseOrderRepository.getRef(dto.getPurchaseOrderId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public PurchaseInvoiceDto update(Long id, PurchaseInvoiceDto dto) {
        // Load current (404 if missing)
        PurchaseInvoice current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("purchaseInvoice not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        PurchaseInvoice replaced = mapper.toEntity(dto);

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
        if (dto.getPurchaseOrderId() != null) { replaced.setPurchaseOrder(purchaseOrderRepository.getRef(dto.getPurchaseOrderId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public PurchaseInvoiceDto patch(Long id, PurchaseInvoiceDto dto) {
        PurchaseInvoice entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("purchaseInvoice not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getPurchaseOrderId() != null) { entity.setPurchaseOrder(purchaseOrderRepository.getRef(dto.getPurchaseOrderId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseInvoiceDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseInvoiceDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseInvoiceDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseInvoiceDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseInvoiceDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("purchaseInvoice not found with id=" + id);
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
    private void precheckCreate(PurchaseInvoiceDto dto) {
        if (dto.getPurchaseOrderId() == null) { throw new MissingRequiredFieldException("purchase_order_id is required"); }
        if (dto.getPurchaseOrderId() != null && !purchaseOrderRepository.existsById(dto.getPurchaseOrderId())) { throw new ForeignKeyNotFoundException("purchase_order_id references missing purchase_order"); }
    }

    private void precheckUpdate(Long id, PurchaseInvoiceDto dto) {
        if (dto.getPurchaseOrderId() == null) { throw new MissingRequiredFieldException("purchase_order_id is required"); }
        if (dto.getPurchaseOrderId() != null && !purchaseOrderRepository.existsById(dto.getPurchaseOrderId())) { throw new ForeignKeyNotFoundException("purchase_order_id references missing purchase_order"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
