package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.PurchaseOrderDto;

public interface PurchaseOrderService {

    PurchaseOrderDto create(PurchaseOrderDto dto);

    PurchaseOrderDto update(Long id, PurchaseOrderDto dto);

    PurchaseOrderDto patch(Long id, PurchaseOrderDto dto);

    Optional<PurchaseOrderDto> findById(Long id);

    Optional<PurchaseOrderDto> findByUuid(UUID uuid);

    List<PurchaseOrderDto> findAll();

    Page<PurchaseOrderDto> findAll(Pageable pageable);

    List<PurchaseOrderDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
