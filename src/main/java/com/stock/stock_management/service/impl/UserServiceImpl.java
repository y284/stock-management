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

import com.stock.stock_management.dto.UserDto;
import com.stock.stock_management.entity.User;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.WarehouseRepository;

import com.stock.stock_management.mapper.UserMapper;
import com.stock.stock_management.repository.UserRepository;
import com.stock.stock_management.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    private final WarehouseRepository warehouseRepository;

    // ========= Create =========
    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        precheckCreate(dto);
        User entity = mapper.toEntity(dto);
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        // Load current (404 if missing)
        User current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("user not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        User replaced = mapper.toEntity(dto);

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
        if (dto.getWarehouseId() != null) { replaced.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public UserDto patch(Long id, UserDto dto) {
        User entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("user not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("user not found with id=" + id);
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
    private void precheckCreate(UserDto dto) {
        if (dto.getUsername() == null) { throw new MissingRequiredFieldException("username is required"); }
        if (dto.getUsername() != null && repository.existsByUsername(dto.getUsername())) { throw new DuplicateResourceException("user with username already exists"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(Long id, UserDto dto) {
        if (dto.getUsername() == null) { throw new MissingRequiredFieldException("username is required"); }
        if (dto.getUsername() != null && repository.existsByUsernameAndIdNot(dto.getUsername(), id)) { throw new DuplicateResourceException("user with username already exists"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
