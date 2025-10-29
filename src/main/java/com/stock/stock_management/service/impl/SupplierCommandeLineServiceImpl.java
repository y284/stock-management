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

import com.stock.stock_management.dto.SupplierCommandeLineDto;
import com.stock.stock_management.entity.SupplierCommandeLine;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.ProductRepository;
import com.stock.stock_management.repository.SupplierCommandeRepository;

import com.stock.stock_management.mapper.SupplierCommandeLineMapper;
import com.stock.stock_management.repository.SupplierCommandeLineRepository;
import com.stock.stock_management.service.SupplierCommandeLineService;

@Service
@RequiredArgsConstructor
public class SupplierCommandeLineServiceImpl implements SupplierCommandeLineService {

    private final SupplierCommandeLineRepository repository;
    private final SupplierCommandeLineMapper mapper;

    private final SupplierCommandeRepository supplierCommandeRepository;
    private final ProductRepository productRepository;

    // ========= Create =========
    @Override
    @Transactional
    public SupplierCommandeLineDto create(SupplierCommandeLineDto dto) {
        precheckCreate(dto);
        SupplierCommandeLine entity = mapper.toEntity(dto);
        if (dto.getSupplierCommandeId() != null) { entity.setSupplierCommande(supplierCommandeRepository.getRef(dto.getSupplierCommandeId())); }
        if (dto.getProductId() != null) { entity.setProduct(productRepository.getRef(dto.getProductId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public SupplierCommandeLineDto update(Long id, SupplierCommandeLineDto dto) {
        // Load current (404 if missing)
        SupplierCommandeLine current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("supplierCommandeLine not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        SupplierCommandeLine replaced = mapper.toEntity(dto);

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
        if (dto.getSupplierCommandeId() != null) { replaced.setSupplierCommande(supplierCommandeRepository.getRef(dto.getSupplierCommandeId())); }
        if (dto.getProductId() != null) { replaced.setProduct(productRepository.getRef(dto.getProductId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public SupplierCommandeLineDto patch(Long id, SupplierCommandeLineDto dto) {
        SupplierCommandeLine entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("supplierCommandeLine not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getSupplierCommandeId() != null) { entity.setSupplierCommande(supplierCommandeRepository.getRef(dto.getSupplierCommandeId())); }
        if (dto.getProductId() != null) { entity.setProduct(productRepository.getRef(dto.getProductId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<SupplierCommandeLineDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SupplierCommandeLineDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierCommandeLineDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierCommandeLineDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierCommandeLineDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("supplierCommandeLine not found with id=" + id);
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
    private void precheckCreate(SupplierCommandeLineDto dto) {
        if (dto.getSupplierCommandeId() == null) { throw new MissingRequiredFieldException("supplier_commande_id is required"); }
        if (dto.getProductId() == null) { throw new MissingRequiredFieldException("product_id is required"); }
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getSupplierCommandeId() != null && !supplierCommandeRepository.existsById(dto.getSupplierCommandeId())) { throw new ForeignKeyNotFoundException("supplier_commande_id references missing supplier_commande"); }
        if (dto.getProductId() != null && !productRepository.existsById(dto.getProductId())) { throw new ForeignKeyNotFoundException("product_id references missing product"); }
    }

    private void precheckUpdate(Long id, SupplierCommandeLineDto dto) {
        if (dto.getSupplierCommandeId() == null) { throw new MissingRequiredFieldException("supplier_commande_id is required"); }
        if (dto.getProductId() == null) { throw new MissingRequiredFieldException("product_id is required"); }
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getSupplierCommandeId() != null && !supplierCommandeRepository.existsById(dto.getSupplierCommandeId())) { throw new ForeignKeyNotFoundException("supplier_commande_id references missing supplier_commande"); }
        if (dto.getProductId() != null && !productRepository.existsById(dto.getProductId())) { throw new ForeignKeyNotFoundException("product_id references missing product"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
