package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SupplierDto;

public interface SupplierService {

    SupplierDto create(SupplierDto dto);

    SupplierDto update(Long id, SupplierDto dto);

    SupplierDto patch(Long id, SupplierDto dto);

    Optional<SupplierDto> findById(Long id);

    Optional<SupplierDto> findByUuid(UUID uuid);

    List<SupplierDto> findAll();

    Page<SupplierDto> findAll(Pageable pageable);

    List<SupplierDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
