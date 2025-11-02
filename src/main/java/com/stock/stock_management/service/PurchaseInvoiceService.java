package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.PurchaseInvoiceDto;

public interface PurchaseInvoiceService {

    PurchaseInvoiceDto create(PurchaseInvoiceDto dto);

    PurchaseInvoiceDto update(Long id, PurchaseInvoiceDto dto);

    PurchaseInvoiceDto patch(Long id, PurchaseInvoiceDto dto);

    Optional<PurchaseInvoiceDto> findById(Long id);

    Optional<PurchaseInvoiceDto> findByUuid(UUID uuid);

    List<PurchaseInvoiceDto> findAll();

    Page<PurchaseInvoiceDto> findAll(Pageable pageable);

    List<PurchaseInvoiceDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
