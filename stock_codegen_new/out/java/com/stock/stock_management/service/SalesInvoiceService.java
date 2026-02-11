package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SalesInvoiceDto;

public interface SalesInvoiceService {

    SalesInvoiceDto create(SalesInvoiceDto dto);

    SalesInvoiceDto update(Long id, SalesInvoiceDto dto);

    SalesInvoiceDto patch(Long id, SalesInvoiceDto dto);

    Optional<SalesInvoiceDto> findById(Long id);

    Optional<SalesInvoiceDto> findByUuid(UUID uuid);

    List<SalesInvoiceDto> findAll();

    Page<SalesInvoiceDto> findAll(Pageable pageable);

    List<SalesInvoiceDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
