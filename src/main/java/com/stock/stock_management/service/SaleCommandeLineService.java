package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SaleCommandeLineDto;

public interface SaleCommandeLineService {

    SaleCommandeLineDto create(SaleCommandeLineDto dto);

    SaleCommandeLineDto update(Long id, SaleCommandeLineDto dto);

    SaleCommandeLineDto patch(Long id, SaleCommandeLineDto dto);

    Optional<SaleCommandeLineDto> findById(Long id);

    Optional<SaleCommandeLineDto> findByUuid(UUID uuid);

    List<SaleCommandeLineDto> findAll();

    Page<SaleCommandeLineDto> findAll(Pageable pageable);

    List<SaleCommandeLineDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
