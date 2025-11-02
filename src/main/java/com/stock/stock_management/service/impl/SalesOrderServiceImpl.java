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

import com.stock.stock_management.dto.SalesOrderDto;
import com.stock.stock_management.entity.SalesOrder;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.ClientRepository;
import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.repository.PaymentRepository;
import com.stock.stock_management.repository.SalesInvoiceRepository;
import com.stock.stock_management.repository.SalesOrderLineRepository;
import com.stock.stock_management.mapper.SalesOrderMapper;
import com.stock.stock_management.repository.SalesOrderRepository;
import com.stock.stock_management.service.SalesOrderService;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository repository;
    private final SalesOrderMapper mapper;

    private final ClientRepository clientRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PaymentRepository paymentRepository;

    // ========= Create =========
    @Override
    @Transactional
    public SalesOrderDto create(SalesOrderDto dto) {
        precheckCreate(dto);
        SalesOrder entity = mapper.toEntity(dto);
        if (dto.getClientId() != null) { entity.setClient(clientRepository.getRef(dto.getClientId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public SalesOrderDto update(Long id, SalesOrderDto dto) {
        // Load current (404 if missing)
        SalesOrder current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("salesOrder not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        SalesOrder replaced = mapper.toEntity(dto);

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
        if (dto.getClientId() != null) { replaced.setClient(clientRepository.getRef(dto.getClientId())); }
        if (dto.getWarehouseId() != null) { replaced.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public SalesOrderDto patch(Long id, SalesOrderDto dto) {
        SalesOrder entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("salesOrder not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getClientId() != null) { entity.setClient(clientRepository.getRef(dto.getClientId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<SalesOrderDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalesOrderDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("salesOrder not found with id=" + id);
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
    private void precheckCreate(SalesOrderDto dto) {
        if (dto.getClientId() == null) { throw new MissingRequiredFieldException("client_id is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getClientId() != null && !clientRepository.existsById(dto.getClientId())) { throw new ForeignKeyNotFoundException("client_id references missing client"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(Long id, SalesOrderDto dto) {
        if (dto.getClientId() == null) { throw new MissingRequiredFieldException("client_id is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getClientId() != null && !clientRepository.existsById(dto.getClientId())) { throw new ForeignKeyNotFoundException("client_id references missing client"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (salesOrderLineRepository.countBySalesOrderId(id) > 0) { throw new ReferentialIntegrityException("salesOrder has dependent sales_order_line records"); }
        if (salesInvoiceRepository.countBySalesOrderId(id) > 0) { throw new ReferentialIntegrityException("salesOrder has dependent sales_invoice records"); }
        if (paymentRepository.countBySalesOrderId(id) > 0) { throw new ReferentialIntegrityException("salesOrder has dependent payment records"); }
    }
}
