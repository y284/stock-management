package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SupplierCommande;

public interface SupplierCommandeRepository extends BaseRepository<SupplierCommande, Long> {

    long countBySupplierId(Long supplierId);
    long countByWarehouseId(Long warehouseId);

}
