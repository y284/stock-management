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

import com.stock.stock_management.dto.SupplierCommandeDto;
import com.stock.stock_management.entity.SupplierCommande;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.SupplierRepository;
import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.repository.InvoiceSupplierRepository;
import com.stock.stock_management.repository.SupplierCommandeLineRepository;
import com.stock.stock_management.mapper.SupplierCommandeMapper;
import com.stock.stock_management.repository.SupplierCommandeRepository;
import com.stock.stock_management.service.SupplierCommandeService;

@Service
@RequiredArgsConstructor
public class SupplierCommandeServiceImpl implements SupplierCommandeService {

    private final SupplierCommandeRepository repository;
    private final SupplierCommandeMapper mapper;

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierCommandeLineRepository supplierCommandeLineRepository;
    private final InvoiceSupplierRepository invoiceSupplierRepository;

    // ========= Create =========
    @Override
    @Transactional
    public SupplierCommandeDto create(SupplierCommandeDto dto) {
        precheckCreate(dto);
        SupplierCommande entity = mapper.toEntity(dto);
        if (dto.getSupplierId() != null) { entity.setSupplier(supplierRepository.getRef(dto.getSupplierId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public SupplierCommandeDto update(Long id, SupplierCommandeDto dto) {
        // Load current (404 if missing)
        SupplierCommande current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("supplierCommande not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        SupplierCommande replaced = mapper.toEntity(dto);

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
    public SupplierCommandeDto patch(Long id, SupplierCommandeDto dto) {
        SupplierCommande entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("supplierCommande not found with id=" + id));

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
    public Optional<SupplierCommandeDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SupplierCommandeDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierCommandeDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierCommandeDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierCommandeDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("supplierCommande not found with id=" + id);
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
    private void precheckCreate(SupplierCommandeDto dto) {
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getSupplierId() != null && !supplierRepository.existsById(dto.getSupplierId())) { throw new ForeignKeyNotFoundException("supplier_id references missing supplier"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(Long id, SupplierCommandeDto dto) {
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getSupplierId() != null && !supplierRepository.existsById(dto.getSupplierId())) { throw new ForeignKeyNotFoundException("supplier_id references missing supplier"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (supplierCommandeLineRepository.countBySupplierCommandeId(id) > 0) { throw new ReferentialIntegrityException("supplierCommande has dependent supplier_commande_line records"); }
        if (invoiceSupplierRepository.countBySupplierCommandeId(id) > 0) { throw new ReferentialIntegrityException("supplierCommande has dependent invoice_supplier records"); }
    }
}
