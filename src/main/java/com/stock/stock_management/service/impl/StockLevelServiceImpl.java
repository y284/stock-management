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

import com.stock.stock_management.dto.StockLevelDto;
import com.stock.stock_management.entity.StockLevel;
import com.stock.stock_management.entity.StockLevelId;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.ProductRepository;
import com.stock.stock_management.repository.WarehouseRepository;

import com.stock.stock_management.mapper.StockLevelMapper;
import com.stock.stock_management.repository.StockLevelRepository;
import com.stock.stock_management.service.StockLevelService;

@Service
@RequiredArgsConstructor
public class StockLevelServiceImpl implements StockLevelService {

    private final StockLevelRepository repository;
    private final StockLevelMapper mapper;

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    // ========= Create =========
    @Override
    @Transactional
    public StockLevelDto create(StockLevelDto dto) {
        precheckCreate(dto);
        StockLevel entity = mapper.toEntity(dto);
        if (dto.getId() != null && dto.getId().getProductId() != null) { entity.setProduct(productRepository.getRef(dto.getId().getProductId())); }
        if (dto.getId() != null && dto.getId().getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getId().getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public StockLevelDto update(StockLevelId id, StockLevelDto dto) {
        // Load current (404 if missing)
        StockLevel current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("stockLevel not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        StockLevel replaced = mapper.toEntity(dto);

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
        replaced.setProduct(productRepository.getRef(id.getProductId()));
        replaced.setWarehouse(warehouseRepository.getRef(id.getWarehouseId()));

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public StockLevelDto patch(StockLevelId id, StockLevelDto dto) {
        StockLevel entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("stockLevel not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getId() != null && dto.getId().getProductId() != null) { entity.setProduct(productRepository.getRef(dto.getId().getProductId())); }
        if (dto.getId() != null && dto.getId().getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getId().getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<StockLevelDto> findById(StockLevelId id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockLevelDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockLevelDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockLevelDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockLevelDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(StockLevelId id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("stockLevel not found with id=" + id);
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
    private void precheckCreate(StockLevelDto dto) {
        if (dto.getId() != null && dto.getId().getProductId() != null && !productRepository.existsById(dto.getId().getProductId())) { throw new ForeignKeyNotFoundException("product_id references missing product"); }
        if (dto.getId() != null && dto.getId().getWarehouseId() != null && !warehouseRepository.existsById(dto.getId().getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(StockLevelId id, StockLevelDto dto) {
        if (!productRepository.existsById(id.getProductId())) { throw new ForeignKeyNotFoundException("product_id references missing product"); }
        if (!warehouseRepository.existsById(id.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(StockLevelId id) { /* no children */ }
}
