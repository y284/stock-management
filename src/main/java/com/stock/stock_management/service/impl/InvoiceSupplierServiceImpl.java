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

import com.stock.stock_management.dto.InvoiceSupplierDto;
import com.stock.stock_management.entity.InvoiceSupplier;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.SupplierCommandeRepository;

import com.stock.stock_management.mapper.InvoiceSupplierMapper;
import com.stock.stock_management.repository.InvoiceSupplierRepository;
import com.stock.stock_management.service.InvoiceSupplierService;

@Service
@RequiredArgsConstructor
public class InvoiceSupplierServiceImpl implements InvoiceSupplierService {

    private final InvoiceSupplierRepository repository;
    private final InvoiceSupplierMapper mapper;

    private final SupplierCommandeRepository supplierCommandeRepository;

    // ========= Create =========
    @Override
    @Transactional
    public InvoiceSupplierDto create(InvoiceSupplierDto dto) {
        precheckCreate(dto);
        InvoiceSupplier entity = mapper.toEntity(dto);
        if (dto.getSupplierCommandeId() != null) { entity.setSupplierCommande(supplierCommandeRepository.getRef(dto.getSupplierCommandeId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public InvoiceSupplierDto update(Long id, InvoiceSupplierDto dto) {
        // Load current (404 if missing)
        InvoiceSupplier current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("invoiceSupplier not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        InvoiceSupplier replaced = mapper.toEntity(dto);

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

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public InvoiceSupplierDto patch(Long id, InvoiceSupplierDto dto) {
        InvoiceSupplier entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("invoiceSupplier not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getSupplierCommandeId() != null) { entity.setSupplierCommande(supplierCommandeRepository.getRef(dto.getSupplierCommandeId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceSupplierDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceSupplierDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSupplierDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSupplierDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceSupplierDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("invoiceSupplier not found with id=" + id);
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
    private void precheckCreate(InvoiceSupplierDto dto) {
        if (dto.getSupplierCommandeId() == null) { throw new MissingRequiredFieldException("supplier_commande_id is required"); }
        if (dto.getSupplierCommandeId() != null && !supplierCommandeRepository.existsById(dto.getSupplierCommandeId())) { throw new ForeignKeyNotFoundException("supplier_commande_id references missing supplier_commande"); }
    }

    private void precheckUpdate(Long id, InvoiceSupplierDto dto) {
        if (dto.getSupplierCommandeId() == null) { throw new MissingRequiredFieldException("supplier_commande_id is required"); }
        if (dto.getSupplierCommandeId() != null && !supplierCommandeRepository.existsById(dto.getSupplierCommandeId())) { throw new ForeignKeyNotFoundException("supplier_commande_id references missing supplier_commande"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
