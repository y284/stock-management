package com.stock.stock_management.repository;

import com.stock.stock_management.entity.PurchaseOrder;

public interface PurchaseOrderRepository extends BaseRepository<PurchaseOrder, Long> {

    long countBySupplierId(Long supplierId);
    long countByWarehouseId(Long warehouseId);

}
