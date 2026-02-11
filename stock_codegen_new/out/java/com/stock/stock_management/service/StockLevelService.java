package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.StockLevelDto;
import com.stock.stock_management.entity.StockLevelId;

public interface StockLevelService {

    StockLevelDto create(StockLevelDto dto);

    StockLevelDto update(StockLevelId id, StockLevelDto dto);

    StockLevelDto patch(StockLevelId id, StockLevelDto dto);

    Optional<StockLevelDto> findById(StockLevelId id);

    Optional<StockLevelDto> findByUuid(UUID uuid);

    List<StockLevelDto> findAll();

    Page<StockLevelDto> findAll(Pageable pageable);

    List<StockLevelDto> findAll(Sort sort);

    void deleteById(StockLevelId id);

    void deleteByUuid(UUID uuid);
}
