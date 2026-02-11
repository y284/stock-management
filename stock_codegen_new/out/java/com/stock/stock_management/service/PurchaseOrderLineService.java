package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.PurchaseOrderLineDto;

public interface PurchaseOrderLineService {

    PurchaseOrderLineDto create(PurchaseOrderLineDto dto);

    PurchaseOrderLineDto update(Long id, PurchaseOrderLineDto dto);

    PurchaseOrderLineDto patch(Long id, PurchaseOrderLineDto dto);

    Optional<PurchaseOrderLineDto> findById(Long id);

    Optional<PurchaseOrderLineDto> findByUuid(UUID uuid);

    List<PurchaseOrderLineDto> findAll();

    Page<PurchaseOrderLineDto> findAll(Pageable pageable);

    List<PurchaseOrderLineDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
