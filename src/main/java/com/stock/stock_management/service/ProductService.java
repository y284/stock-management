package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.ProductDto;

public interface ProductService {

    ProductDto create(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);

    ProductDto patch(Long id, ProductDto dto);

    Optional<ProductDto> findById(Long id);

    Optional<ProductDto> findByUuid(UUID uuid);

    List<ProductDto> findAll();

    Page<ProductDto> findAll(Pageable pageable);

    List<ProductDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
