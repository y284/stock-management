package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SalesOrderDto;

public interface SalesOrderService {

    SalesOrderDto create(SalesOrderDto dto);

    SalesOrderDto update(Long id, SalesOrderDto dto);

    SalesOrderDto patch(Long id, SalesOrderDto dto);

    Optional<SalesOrderDto> findById(Long id);

    Optional<SalesOrderDto> findByUuid(UUID uuid);

    List<SalesOrderDto> findAll();

    Page<SalesOrderDto> findAll(Pageable pageable);

    List<SalesOrderDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
