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

import com.stock.stock_management.dto.ProductDto;
import com.stock.stock_management.entity.Product;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.CategoryRepository;
import com.stock.stock_management.repository.PurchaseOrderLineRepository;
import com.stock.stock_management.repository.SalesOrderLineRepository;
import com.stock.stock_management.repository.StockLevelRepository;
import com.stock.stock_management.mapper.ProductMapper;
import com.stock.stock_management.repository.ProductRepository;
import com.stock.stock_management.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    private final CategoryRepository categoryRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final StockLevelRepository stockLevelRepository;

    // ========= Create =========
    @Override
    @Transactional
    public ProductDto create(ProductDto dto) {
        precheckCreate(dto);
        Product entity = mapper.toEntity(dto);
        if (dto.getCategoryId() != null) { entity.setCategory(categoryRepository.getRef(dto.getCategoryId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        // Load current (404 if missing)
        Product current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("product not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Product replaced = mapper.toEntity(dto);

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
        if (dto.getCategoryId() != null) { replaced.setCategory(categoryRepository.getRef(dto.getCategoryId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public ProductDto patch(Long id, ProductDto dto) {
        Product entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("product not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getCategoryId() != null) { entity.setCategory(categoryRepository.getRef(dto.getCategoryId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("product not found with id=" + id);
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
    private void precheckCreate(ProductDto dto) {
        if (dto.getSku() == null) { throw new MissingRequiredFieldException("sku is required"); }
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getSku() != null && repository.existsBySku(dto.getSku())) { throw new DuplicateResourceException("product with sku already exists"); }
        if (dto.getCategoryId() != null && !categoryRepository.existsById(dto.getCategoryId())) { throw new ForeignKeyNotFoundException("category_id references missing category"); }
    }

    private void precheckUpdate(Long id, ProductDto dto) {
        if (dto.getSku() == null) { throw new MissingRequiredFieldException("sku is required"); }
        if (dto.getName() == null) { throw new MissingRequiredFieldException("name is required"); }
        if (dto.getSku() != null && repository.existsBySkuAndIdNot(dto.getSku(), id)) { throw new DuplicateResourceException("product with sku already exists"); }
        if (dto.getCategoryId() != null && !categoryRepository.existsById(dto.getCategoryId())) { throw new ForeignKeyNotFoundException("category_id references missing category"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (purchaseOrderLineRepository.countByProductId(id) > 0) { throw new ReferentialIntegrityException("product has dependent purchase_order_line records"); }
        if (salesOrderLineRepository.countByProductId(id) > 0) { throw new ReferentialIntegrityException("product has dependent sales_order_line records"); }
        if (stockLevelRepository.countByProductId(id) > 0) { throw new ReferentialIntegrityException("product has dependent stock_level records"); }
    }
}
