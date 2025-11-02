package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SalesOrderLine;

public interface SalesOrderLineRepository extends BaseRepository<SalesOrderLine, Long> {

    long countBySalesOrderId(Long salesOrderId);
    long countByProductId(Long productId);

}
