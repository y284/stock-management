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

import com.stock.stock_management.dto.CategoryDto;
import com.stock.stock_management.entity.Category;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.CategoryRepository;
import com.stock.stock_management.repository.ProductRepository;
import com.stock.stock_management.mapper.CategoryMapper;
import com.stock.stock_management.service.CategoryService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    private final ProductRepository productRepository;

    // ========= Create =========
    @Override
    @Transactional
    public CategoryDto create(CategoryDto dto) {
        precheckCreate(dto);
        Category entity = mapper.toEntity(dto);
        if (dto.getParentId() != null) { entity.setParent(repository.getRef(dto.getParentId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        // Load current (404 if missing)
        Category current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("category not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Category replaced = mapper.toEntity(dto);

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
        if (dto.getParentId() != null) { replaced.setParent(repository.getRef(dto.getParentId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public CategoryDto patch(Long id, CategoryDto dto) {
        Category entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("category not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getParentId() != null) { entity.setParent(repository.getRef(dto.getParentId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("category not found with id=" + id);
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
    private void precheckCreate(CategoryDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getName() != null && repository.existsByName(dto.getName())) { throw new DuplicateResourceException("category with name already exists"); }
        if (dto.getParentId() != null && !repository.existsById(dto.getParentId())) { throw new ForeignKeyNotFoundException("parent_id references missing category"); }
    }

    private void precheckUpdate(Long id, CategoryDto dto) {
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getName() != null && repository.existsByNameAndIdNot(dto.getName(), id)) { throw new DuplicateResourceException("category with name already exists"); }
        if (dto.getParentId() != null && !repository.existsById(dto.getParentId())) { throw new ForeignKeyNotFoundException("parent_id references missing category"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (repository.countByParentId(id) > 0) { throw new ReferentialIntegrityException("category has dependent category records"); }
        if (productRepository.countByCategoryId(id) > 0) { throw new ReferentialIntegrityException("category has dependent product records"); }
    }
}
