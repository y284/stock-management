package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SupplierCommandeLineDto;

public interface SupplierCommandeLineService {

    SupplierCommandeLineDto create(SupplierCommandeLineDto dto);

    SupplierCommandeLineDto update(Long id, SupplierCommandeLineDto dto);

    SupplierCommandeLineDto patch(Long id, SupplierCommandeLineDto dto);

    Optional<SupplierCommandeLineDto> findById(Long id);

    Optional<SupplierCommandeLineDto> findByUuid(UUID uuid);

    List<SupplierCommandeLineDto> findAll();

    Page<SupplierCommandeLineDto> findAll(Pageable pageable);

    List<SupplierCommandeLineDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
