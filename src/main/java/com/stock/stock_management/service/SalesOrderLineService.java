package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SalesOrderLineDto;

public interface SalesOrderLineService {

    SalesOrderLineDto create(SalesOrderLineDto dto);

    SalesOrderLineDto update(Long id, SalesOrderLineDto dto);

    SalesOrderLineDto patch(Long id, SalesOrderLineDto dto);

    Optional<SalesOrderLineDto> findById(Long id);

    Optional<SalesOrderLineDto> findByUuid(UUID uuid);

    List<SalesOrderLineDto> findAll();

    Page<SalesOrderLineDto> findAll(Pageable pageable);

    List<SalesOrderLineDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
