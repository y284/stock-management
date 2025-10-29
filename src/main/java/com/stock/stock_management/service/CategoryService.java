package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.CategoryDto;

public interface CategoryService {

    CategoryDto create(CategoryDto dto);

    CategoryDto update(Long id, CategoryDto dto);

    CategoryDto patch(Long id, CategoryDto dto);

    Optional<CategoryDto> findById(Long id);

    Optional<CategoryDto> findByUuid(UUID uuid);

    List<CategoryDto> findAll();

    Page<CategoryDto> findAll(Pageable pageable);

    List<CategoryDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
