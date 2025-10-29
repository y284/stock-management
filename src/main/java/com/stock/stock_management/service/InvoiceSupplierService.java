package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.InvoiceSupplierDto;

public interface InvoiceSupplierService {

    InvoiceSupplierDto create(InvoiceSupplierDto dto);

    InvoiceSupplierDto update(Long id, InvoiceSupplierDto dto);

    InvoiceSupplierDto patch(Long id, InvoiceSupplierDto dto);

    Optional<InvoiceSupplierDto> findById(Long id);

    Optional<InvoiceSupplierDto> findByUuid(UUID uuid);

    List<InvoiceSupplierDto> findAll();

    Page<InvoiceSupplierDto> findAll(Pageable pageable);

    List<InvoiceSupplierDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
