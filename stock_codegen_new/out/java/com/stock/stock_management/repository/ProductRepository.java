package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Product;

public interface ProductRepository extends BaseRepository<Product, Long> {

    boolean existsByDescription(String description);
    boolean existsByDescriptionAndIdNot(String description, Long id);
    java.util.Optional<Product> findByDescriptionIgnoreCase(String description);
    long countByCategoryId(Long categoryId);

}
