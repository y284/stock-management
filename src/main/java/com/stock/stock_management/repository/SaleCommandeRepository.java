package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SaleCommande;

public interface SaleCommandeRepository extends BaseRepository<SaleCommande, Long> {

    long countByClientId(Long clientId);
    long countByWarehouseId(Long warehouseId);

}
