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

import com.stock.stock_management.dto.EnterpriseDto;
import com.stock.stock_management.entity.Enterprise;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;

import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.mapper.EnterpriseMapper;
import com.stock.stock_management.repository.EnterpriseRepository;
import com.stock.stock_management.service.EnterpriseService;

@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseRepository repository;
    private final EnterpriseMapper mapper;

    private final WarehouseRepository warehouseRepository;

    // ========= Create =========
    @Override
    @Transactional
    public EnterpriseDto create(EnterpriseDto dto) {
        precheckCreate(dto);
        Enterprise entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public EnterpriseDto update(Long id, EnterpriseDto dto) {
        // Load current (404 if missing)
        Enterprise current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("enterprise not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Enterprise replaced = mapper.toEntity(dto);

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

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public EnterpriseDto patch(Long id, EnterpriseDto dto) {
        Enterprise entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("enterprise not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<EnterpriseDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnterpriseDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnterpriseDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnterpriseDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnterpriseDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("enterprise not found with id=" + id);
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
    private void precheckCreate(EnterpriseDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getName() != null && repository.existsByName(dto.getName())) { throw new DuplicateResourceException("enterprise with name already exists"); }
    }

    private void precheckUpdate(Long id, EnterpriseDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getName() != null && repository.existsByNameAndIdNot(dto.getName(), id)) { throw new DuplicateResourceException("enterprise with name already exists"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (warehouseRepository.countByEnterpriseId(id) > 0) { throw new ReferentialIntegrityException("enterprise has dependent warehouse records"); }
    }
}
