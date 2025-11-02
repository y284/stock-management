package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.EnterpriseDto;

public interface EnterpriseService {

    EnterpriseDto create(EnterpriseDto dto);

    EnterpriseDto update(Long id, EnterpriseDto dto);

    EnterpriseDto patch(Long id, EnterpriseDto dto);

    Optional<EnterpriseDto> findById(Long id);

    Optional<EnterpriseDto> findByUuid(UUID uuid);

    List<EnterpriseDto> findAll();

    Page<EnterpriseDto> findAll(Pageable pageable);

    List<EnterpriseDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
