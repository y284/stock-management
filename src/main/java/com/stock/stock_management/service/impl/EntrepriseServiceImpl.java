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

import com.stock.stock_management.dto.EntrepriseDto;
import com.stock.stock_management.entity.Entreprise;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;

import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.mapper.EntrepriseMapper;
import com.stock.stock_management.repository.EntrepriseRepository;
import com.stock.stock_management.service.EntrepriseService;

@Service
@RequiredArgsConstructor
public class EntrepriseServiceImpl implements EntrepriseService {

    private final EntrepriseRepository repository;
    private final EntrepriseMapper mapper;

    private final WarehouseRepository warehouseRepository;

    // ========= Create =========
    @Override
    @Transactional
    public EntrepriseDto create(EntrepriseDto dto) {
        precheckCreate(dto);
        Entreprise entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public EntrepriseDto update(Long id, EntrepriseDto dto) {
        // Load current (404 if missing)
        Entreprise current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("entreprise not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Entreprise replaced = mapper.toEntity(dto);

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
    public EntrepriseDto patch(Long id, EntrepriseDto dto) {
        Entreprise entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("entreprise not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<EntrepriseDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntrepriseDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntrepriseDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntrepriseDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntrepriseDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("entreprise not found with id=" + id);
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
    private void precheckCreate(EntrepriseDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getName() != null && repository.existsByName(dto.getName())) { throw new DuplicateResourceException("entreprise with name already exists"); }
    }

    private void precheckUpdate(Long id, EntrepriseDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getName() != null && repository.existsByNameAndIdNot(dto.getName(), id)) { throw new DuplicateResourceException("entreprise with name already exists"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (warehouseRepository.countByEntrepriseId(id) > 0) { throw new ReferentialIntegrityException("entreprise has dependent warehouse records"); }
    }
}
