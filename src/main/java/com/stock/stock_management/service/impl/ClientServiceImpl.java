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

import com.stock.stock_management.dto.ClientDto;
import com.stock.stock_management.entity.Client;

import com.stock.stock_management.error.ResourceNotFoundException;
import com.stock.stock_management.error.DuplicateResourceException;
import com.stock.stock_management.error.ForeignKeyNotFoundException;
import com.stock.stock_management.error.MissingRequiredFieldException;
import com.stock.stock_management.error.ReferentialIntegrityException;
import com.stock.stock_management.repository.WarehouseRepository;
import com.stock.stock_management.repository.SaleCommandeRepository;
import com.stock.stock_management.mapper.ClientMapper;
import com.stock.stock_management.repository.ClientRepository;
import com.stock.stock_management.service.ClientService;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;

    private final WarehouseRepository warehouseRepository;
    private final SaleCommandeRepository saleCommandeRepository;

    // ========= Create =========
    @Override
    @Transactional
    public ClientDto create(ClientDto dto) {
        precheckCreate(dto);
        Client entity = mapper.toEntity(dto);
        if (dto.getWarehouseId() != null) { entity.setWarehouse(warehouseRepository.getRef(dto.getWarehouseId())); }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public ClientDto update(Long id, ClientDto dto) {
        // Load current (404 if missing)
        Client current = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("client not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        Client replaced = mapper.toEntity(dto);

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
    public ClientDto patch(Long id, ClientDto dto) {
        Client entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("client not found with id=" + id));

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
    public Optional<ClientDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientDto> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> findAll(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("client not found with id=" + id);
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
    private void precheckCreate(ClientDto dto) {
        if (dto.getFullname() == null) { throw new MissingRequiredFieldException("fullname is required"); }
        if (dto.getRib() == null) { throw new MissingRequiredFieldException("rib is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getEmail() != null && repository.existsByEmail(dto.getEmail())) { throw new DuplicateResourceException("client with email already exists"); }
        if (dto.getRib() != null && repository.existsByRib(dto.getRib())) { throw new DuplicateResourceException("client with rib already exists"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    private void precheckUpdate(Long id, ClientDto dto) {
        if (dto.getFullname() == null) { throw new MissingRequiredFieldException("fullname is required"); }
        if (dto.getRib() == null) { throw new MissingRequiredFieldException("rib is required"); }
        if (dto.getWarehouseId() == null) { throw new MissingRequiredFieldException("warehouse_id is required"); }
        if (dto.getEmail() != null && repository.existsByEmailAndIdNot(dto.getEmail(), id)) { throw new DuplicateResourceException("client with email already exists"); }
        if (dto.getRib() != null && repository.existsByRibAndIdNot(dto.getRib(), id)) { throw new DuplicateResourceException("client with rib already exists"); }
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) { throw new ForeignKeyNotFoundException("warehouse_id references missing warehouse"); }
    }

    // ========= Delete guard (child refs) =========
    private void guardDelete(Long id) {
        if (saleCommandeRepository.countByClientId(id) > 0) { throw new ReferentialIntegrityException("client has dependent sale_commande records"); }
    }
}
