package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.InvoiceClientDto;

public interface InvoiceClientService {

    InvoiceClientDto create(InvoiceClientDto dto);

    InvoiceClientDto update(Long id, InvoiceClientDto dto);

    InvoiceClientDto patch(Long id, InvoiceClientDto dto);

    Optional<InvoiceClientDto> findById(Long id);

    Optional<InvoiceClientDto> findByUuid(UUID uuid);

    List<InvoiceClientDto> findAll();

    Page<InvoiceClientDto> findAll(Pageable pageable);

    List<InvoiceClientDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
