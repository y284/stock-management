package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SaleCommandeLine;

public interface SaleCommandeLineRepository extends BaseRepository<SaleCommandeLine, Long> {

    long countBySaleCommandeId(Long saleCommandeId);
    long countByProductId(Long productId);

}
