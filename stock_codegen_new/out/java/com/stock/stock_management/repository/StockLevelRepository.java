package com.stock.stock_management.repository;

import com.stock.stock_management.entity.StockLevel;
import com.stock.stock_management.entity.StockLevelId;

public interface StockLevelRepository extends BaseRepository<StockLevel, StockLevelId> {

    long countByProductId(Long productId);
    long countByWarehouseId(Long warehouseId);

}
