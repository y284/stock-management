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

import com.stock.stock_management.dto.WarehouseDto;
import com.stock.stock_management.entity.Warehouse;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.EnterpriseRepository;
import com.stock.stock_management.repository.ClientRepository;
import com.stock.stock_management.repository.PurchaseOrderRepository;
import com.stock.stock_management.repository.SalesOrderRepository;
import com.stock.stock_management.repository.StockLevelRepository;
import com.stock.stock_management.repository.SupplierRepository;
import com.stock.stock_management.repository.UserRepository;
import com.stock.stock_management.mapper.WarehouseMapper;
import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.service.WarehouseService;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;

    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final ClientRepository clientRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StockLevelRepository stockLevelRepository;

    // ========= Create =========
    @Override
    @Transactional
    public WarehouseDto create(WarehouseDto dto) {
        precheckCreate(dto);
        Warehouse entity = mapper.toEntity(dto);
        if (dto.getEnterpriseId() != null) { entity.setEnterprise(enterpriseRepository.getRef(dto.getEnterpriseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public WarehouseDto update(Long id, WarehouseDto dto) {
        // Load current (404 if missing)
        Warehouse current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("warehouse not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Warehouse replaced = mapper.toEntity(dto);

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
        if (dto.getEnterpriseId() != null) { replaced.setEnterprise(enterpriseRepository.getRef(dto.getEnterpriseId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public WarehouseDto patch(Long id, WarehouseDto dto) {
        Warehouse entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("warehouse not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getEnterpriseId() != null) { entity.setEnterprise(enterpriseRepository.getRef(dto.getEnterpriseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("warehouse not found with id=" + id);
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
    private void precheckCreate(WarehouseDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getCode() == null) { throw new MissingRequiredFieldException("code is required"); }
        if (dto.getEnterpriseId() == null) { throw new MissingRequiredFieldException("enterprise_id is required"); }
        if (dto.getCode() != null && repository.existsByCode(dto.getCode())) { throw new DuplicateResourceException("warehouse with code already exists"); }
        if (dto.getEnterpriseId() != null && !enterpriseRepository.existsById(dto.getEnterpriseId())) { throw new ForeignKeyNotFoundException("enterprise_id references missing enterprise"); }
    }

    private void precheckUpdate(Long id, WarehouseDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getCode() == null) { throw new MissingRequiredFieldException("code is required"); }
        if (dto.getEnterpriseId() == null) { throw new MissingRequiredFieldException("enterprise_id is required"); }
        if (dto.getCode() != null && repository.existsByCodeAndIdNot(dto.getCode(), id)) { throw new DuplicateResourceException("warehouse with code already exists"); }
        if (dto.getEnterpriseId() != null && !enterpriseRepository.existsById(dto.getEnterpriseId())) { throw new ForeignKeyNotFoundException("enterprise_id references missing enterprise"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (userRepository.countByWarehouseId(id) > 0) { throw new ReferentialIntegrityException("warehouse has dependent user records"); }
        if (supplierRepository.countByWarehouseId(id) > 0) { throw new ReferentialIntegrityException("warehouse has dependent supplier records"); }
        if (clientRepository.countByWarehouseId(id) > 0) { throw new ReferentialIntegrityException("warehouse has dependent client records"); }
        if (purchaseOrderRepository.countByWarehouseId(id) > 0) { throw new ReferentialIntegrityException("warehouse has dependent purchase_order records"); }
        if (salesOrderRepository.countByWarehouseId(id) > 0) { throw new ReferentialIntegrityException("warehouse has dependent sales_order records"); }
        if (stockLevelRepository.countByWarehouseId(id) > 0) { throw new ReferentialIntegrityException("warehouse has dependent stock_level records"); }
    }
}
