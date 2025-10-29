package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.EntrepriseDto;

public interface EntrepriseService {

    EntrepriseDto create(EntrepriseDto dto);

    EntrepriseDto update(Long id, EntrepriseDto dto);

    EntrepriseDto patch(Long id, EntrepriseDto dto);

    Optional<EntrepriseDto> findById(Long id);

    Optional<EntrepriseDto> findByUuid(UUID uuid);

    List<EntrepriseDto> findAll();

    Page<EntrepriseDto> findAll(Pageable pageable);

    List<EntrepriseDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
