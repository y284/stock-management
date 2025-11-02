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

import com.stock.stock_management.dto.PurchaseOrderDto;
import com.stock.stock_management.entity.PurchaseOrder;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.SupplierRepository;
import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.repository.PurchaseInvoiceRepository;
import com.stock.stock_management.repository.PurchaseOrderLineRepository;
import com.stock.stock_management.mapper.PurchaseOrderMapper;
import com.stock.stock_management.repository.PurchaseOrderRepository;
import com.stock.stock_management.service.PurchaseOrderService;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository repository;
    private final PurchaseOrderMapper mapper;

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;

    // ========= Create =========
    @Override
    @Transactional
    public PurchaseOrderDto create(PurchaseOrderDto dto) {
        precheckCreate(dto);
        PurchaseOrder entity = mapper.toEntity(dto);
        if (dto.getSupplierId() != null) { entity.setSupplier(supplierRepository.getRef(dto.getSupplierId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public PurchaseOrderDto update(Long id, PurchaseOrderDto dto) {
        // Load current (404 if missing)
        PurchaseOrder current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("purchaseOrder not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        PurchaseOrder replaced = mapper.toEntity(dto);

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
        if (dto.getSupplierId() != null) { replaced.setSupplier(supplierRepository.getRef(dto.getSupplierId())); }
        if (dto.getWarehouseId() != null) { replaced.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public PurchaseOrderDto patch(Long id, PurchaseOrderDto dto) {
        PurchaseOrder entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("purchaseOrder not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getSupplierId() != null) { entity.setSupplier(supplierRepository.getRef(dto.getSupplierId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("purchaseOrder not found with id=" + id);
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
    private void precheckCreate(PurchaseOrderDto dto) {
        if (dto.getSupplierId() == null) { throw new MissingRequiredFieldException("supplier_id is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getSupplierId() != null && !supplierRepository.existsById(dto.getSupplierId())) { throw new ForeignKeyNotFoundException("supplier_id references missing supplier"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(Long id, PurchaseOrderDto dto) {
        if (dto.getSupplierId() == null) { throw new MissingRequiredFieldException("supplier_id is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getSupplierId() != null && !supplierRepository.existsById(dto.getSupplierId())) { throw new ForeignKeyNotFoundException("supplier_id references missing supplier"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (purchaseOrderLineRepository.countByPurchaseOrderId(id) > 0) { throw new ReferentialIntegrityException("purchaseOrder has dependent purchase_order_line records"); }
        if (purchaseInvoiceRepository.countByPurchaseOrderId(id) > 0) { throw new ReferentialIntegrityException("purchaseOrder has dependent purchase_invoice records"); }
    }
}
