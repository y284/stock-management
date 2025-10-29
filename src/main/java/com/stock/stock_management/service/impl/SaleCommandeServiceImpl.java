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

import com.stock.stock_management.dto.SaleCommandeDto;
import com.stock.stock_management.entity.SaleCommande;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.ClientRepository;
import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.repository.InvoiceClientRepository;
import com.stock.stock_management.repository.SaleCommandeLineRepository;
import com.stock.stock_management.mapper.SaleCommandeMapper;
import com.stock.stock_management.repository.SaleCommandeRepository;
import com.stock.stock_management.service.SaleCommandeService;

@Service
@RequiredArgsConstructor
public class SaleCommandeServiceImpl implements SaleCommandeService {

    private final SaleCommandeRepository repository;
    private final SaleCommandeMapper mapper;

    private final ClientRepository clientRepository;
    private final WarehouseRepository warehouseRepository;
    private final SaleCommandeLineRepository saleCommandeLineRepository;
    private final InvoiceClientRepository invoiceClientRepository;

    // ========= Create =========
    @Override
    @Transactional
    public SaleCommandeDto create(SaleCommandeDto dto) {
        precheckCreate(dto);
        SaleCommande entity = mapper.toEntity(dto);
        if (dto.getClientId() != null) { entity.setClient(clientRepository.getRef(dto.getClientId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public SaleCommandeDto update(Long id, SaleCommandeDto dto) {
        // Load current (404 if missing)
        SaleCommande current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("saleCommande not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        SaleCommande replaced = mapper.toEntity(dto);

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
        if (dto.getClientId() != null) { replaced.setClient(clientRepository.getRef(dto.getClientId())); }
        if (dto.getWarehouseId() != null) { replaced.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public SaleCommandeDto patch(Long id, SaleCommandeDto dto) {
        SaleCommande entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("saleCommande not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);
        if (dto.getClientId() != null) { entity.setClient(clientRepository.getRef(dto.getClientId())); }
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<SaleCommandeDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SaleCommandeDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleCommandeDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleCommandeDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleCommandeDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("saleCommande not found with id=" + id);
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
    private void precheckCreate(SaleCommandeDto dto) {
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getClientId() == null) { throw new MissingRequiredFieldException("client_id is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getClientId() != null && !clientRepository.existsById(dto.getClientId())) { throw new ForeignKeyNotFoundException("client_id references missing client"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(Long id, SaleCommandeDto dto) {
        if (dto.getQuantity() == null) { throw new MissingRequiredFieldException("quantity is required"); }
        if (dto.getPrice() == null) { throw new MissingRequiredFieldException("price is required"); }
        if (dto.getClientId() == null) { throw new MissingRequiredFieldException("client_id is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getClientId() != null && !clientRepository.existsById(dto.getClientId())) { throw new ForeignKeyNotFoundException("client_id references missing client"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (saleCommandeLineRepository.countBySaleCommandeId(id) > 0) { throw new ReferentialIntegrityException("saleCommande has dependent sale_commande_line records"); }
        if (invoiceClientRepository.countBySaleCommandeId(id) > 0) { throw new ReferentialIntegrityException("saleCommande has dependent invoice_client records"); }
    }
}
