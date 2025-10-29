package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Product;

public interface ProductRepository extends BaseRepository<Product, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    java.util.Optional<Product> findBySkuIgnoreCase(String sku);
    long countByCategoryId(Long categoryId);

}
