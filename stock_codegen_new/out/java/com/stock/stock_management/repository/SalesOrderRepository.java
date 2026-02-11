package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SalesOrder;

public interface SalesOrderRepository extends BaseRepository<SalesOrder, Long> {

    long countByClientId(Long clientId);
    long countByWarehouseId(Long warehouseId);

}
