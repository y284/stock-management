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

import com.stock.stock_management.dto.InvoiceClientDto;
import com.stock.stock_management.entity.InvoiceClient;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.SaleCommandeRepository;

import com.stock.stock_management.mapper.InvoiceClientMapper;
import com.stock.stock_management.repository.InvoiceClientRepository;
import com.stock.stock_management.service.InvoiceClientService;

@Service
@RequiredArgsConstructor
public class InvoiceClientServiceImpl implements InvoiceClientService {

    private final InvoiceClientRepository repository;
    private final InvoiceClientMapper mapper;

    private final SaleCommandeRepository saleCommandeRepository;

    // ========= Create =========
    @Override
    @Transactional
    public InvoiceClientDto create(InvoiceClientDto dto) {
        precheckCreate(dto);
        InvoiceClient entity = mapper.toEntity(dto);
        if (dto.getSaleCommandeId() != null) { entity.setSaleCommande(saleCommandeRepository.getRef(dto.getSaleCommandeId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public InvoiceClientDto update(Long id, InvoiceClientDto dto) {
        // Load current (404 if missing)
        InvoiceClient current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("invoiceClient not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        InvoiceClient replaced = mapper.toEntity(dto);

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

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public InvoiceClientDto patch(Long id, InvoiceClientDto dto) {
        InvoiceClient entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("invoiceClient not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getSaleCommandeId() != null) { entity.setSaleCommande(saleCommandeRepository.getRef(dto.getSaleCommandeId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceClientDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceClientDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceClientDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceClientDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceClientDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("invoiceClient not found with id=" + id);
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
    private void precheckCreate(InvoiceClientDto dto) {
        if (dto.getSaleCommandeId() == null) { throw new MissingRequiredFieldException("sale_commande_id is required"); }
        if (dto.getSaleCommandeId() != null && !saleCommandeRepository.existsById(dto.getSaleCommandeId())) { throw new ForeignKeyNotFoundException("sale_commande_id references missing sale_commande"); }
    }

    private void precheckUpdate(Long id, InvoiceClientDto dto) {
        if (dto.getSaleCommandeId() == null) { throw new MissingRequiredFieldException("sale_commande_id is required"); }
        if (dto.getSaleCommandeId() != null && !saleCommandeRepository.existsById(dto.getSaleCommandeId())) { throw new ForeignKeyNotFoundException("sale_commande_id references missing sale_commande"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) { /* no children */ }
}
