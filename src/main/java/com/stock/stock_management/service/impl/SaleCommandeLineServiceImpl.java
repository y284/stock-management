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

import com.stock.stock_management.dto.SaleCommandeLineDto;
import com.stock.stock_management.entity.SaleCommandeLine;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.ProductRepository;
import com.stock.stock_management.repository.SaleCommandeRepository;

import com.stock.stock_management.mapper.SaleCommandeLineMapper;
import com.stock.stock_management.repository.SaleCommandeLineRepository;
import com.stock.stock_management.service.SaleCommandeLineService;

@Service
@RequiredArgsConstructor
public class SaleCommandeLineServiceImpl implements SaleCommandeLineService {

    private final SaleCommandeLineRepository repository;
    private final SaleCommandeLineMapper mapper;

    private final SaleCommandeRepository saleCommandeRepository;
    private final ProductRepository productRepository;

    // ========= Create =========
    @Override
    @Transactional
    public SaleCommandeLineDto create(SaleCommandeLineDto dto) {
        precheckCreate(dto);
        SaleCommandeLine entity = mapper.toEntity(dto);
        if (dto.getSaleCommandeId() != null) { entity.setSaleCommande(saleCommandeRepository.getRef(dto.getSaleCommandeId())); }
        if (dto.getProductId() != null) { entity.setProduct(productRepository.getRef(dto.getProductId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public SaleCommandeLineDto update(Long id, SaleCommandeLineDto dto) {
        // Load current (404 if missing)
        SaleCommandeLine current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("saleCommandeLine not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        SaleCommandeLine replaced = mapper.toEntity(dto);

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
        if (dto.getSaleCommandeId() != null) { replaced.setSaleCommande(saleCommandeRepository.getRef(dto.getSaleCommandeId())); }
        if (dto.getProductId() != null) { replaced.setProduct(productRepository.getRef(dto.getProductId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public SaleCommandeLineDto patch(Long id, SaleCommandeLineDto dto) {
        SaleCommandeLine entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("saleCommandeLine not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getSaleCommandeId() != null) { entity.setSaleCommande(saleCommandeRepository.getRef(dto.getSaleCommandeId())); }
        if (dto.getProductId() != null) { entity.setProduct(productRepository.getRef(dto.getProductId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<SaleCommandeLineDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SaleCommandeLineDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleCommandeLineDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleCommandeLineDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleCommandeLineDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("saleCommandeLine not found with id=" + id);
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
    private void precheckCreate(SaleCommandeLineDto dto) {
        if (dto.getSaleCommandeId() == null) { throw new MissingRequiredFieldException("sale_commande_id is required"); }
        if (dto.getProductId() == null) { throw new MissingRequiredFieldException("product_id is required"); }
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getSaleCommandeId() != null && !saleCommandeRepository.existsById(dto.getSaleCommandeId())) { throw new ForeignKeyNotFoundException("sale_commande_id references missing sale_commande"); }
        if (dto.getProductId() != null && !productRepository.existsById(dto.getProductId())) { throw new ForeignKeyNotFoundException("product_id references missing product"); }
    }

    private void precheckUpdate(Long id, SaleCommandeLineDto dto) {
        if (dto.getSaleCommandeId() == null) { throw new MissingRequiredFieldException("sale_commande_id is required"); }
        if (dto.getProductId() == null) { throw new MissingRequiredFieldException("product_id is required"); }
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getSaleCommandeId() != null && !saleCommandeRepository.existsById(dto.getSaleCommandeId())) { throw new ForeignKeyNotFoundException("sale_commande_id references missing sale_commande"); }
        if (dto.getProductId() != null && !productRepository.existsById(dto.getProductId())) { throw new ForeignKeyNotFoundException("product_id references missing product"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
