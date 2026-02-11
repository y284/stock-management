package com.stock.stock_management.repository;

import com.stock.stock_management.entity.PurchaseOrderLine;

public interface PurchaseOrderLineRepository extends BaseRepository<PurchaseOrderLine, Long> {

    long countByPurchaseOrderId(Long purchaseOrderId);
    long countByProductId(Long productId);

}
