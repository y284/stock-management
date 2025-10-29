package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SupplierCommandeDto;

public interface SupplierCommandeService {

    SupplierCommandeDto create(SupplierCommandeDto dto);

    SupplierCommandeDto update(Long id, SupplierCommandeDto dto);

    SupplierCommandeDto patch(Long id, SupplierCommandeDto dto);

    Optional<SupplierCommandeDto> findById(Long id);

    Optional<SupplierCommandeDto> findByUuid(UUID uuid);

    List<SupplierCommandeDto> findAll();

    Page<SupplierCommandeDto> findAll(Pageable pageable);

    List<SupplierCommandeDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
