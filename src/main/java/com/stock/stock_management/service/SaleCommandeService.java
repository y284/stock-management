package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.SaleCommandeDto;

public interface SaleCommandeService {

    SaleCommandeDto create(SaleCommandeDto dto);

    SaleCommandeDto update(Long id, SaleCommandeDto dto);

    SaleCommandeDto patch(Long id, SaleCommandeDto dto);

    Optional<SaleCommandeDto> findById(Long id);

    Optional<SaleCommandeDto> findByUuid(UUID uuid);

    List<SaleCommandeDto> findAll();

    Page<SaleCommandeDto> findAll(Pageable pageable);

    List<SaleCommandeDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
