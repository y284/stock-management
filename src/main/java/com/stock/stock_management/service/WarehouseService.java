package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.WarehouseDto;

public interface WarehouseService {

    WarehouseDto create(WarehouseDto dto);

    WarehouseDto update(Long id, WarehouseDto dto);

    WarehouseDto patch(Long id, WarehouseDto dto);

    Optional<WarehouseDto> findById(Long id);

    Optional<WarehouseDto> findByUuid(UUID uuid);

    List<WarehouseDto> findAll();

    Page<WarehouseDto> findAll(Pageable pageable);

    List<WarehouseDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
