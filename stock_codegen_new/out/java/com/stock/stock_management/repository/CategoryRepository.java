package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Category;

public interface CategoryRepository extends BaseRepository<Category, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    java.util.Optional<Category> findByNameIgnoreCase(String name);
    long countByParentId(Long parentId);

}
