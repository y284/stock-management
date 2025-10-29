package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SupplierCommandeLine;

public interface SupplierCommandeLineRepository extends BaseRepository<SupplierCommandeLine, Long> {

    long countBySupplierCommandeId(Long supplierCommandeId);
    long countByProductId(Long productId);

}
