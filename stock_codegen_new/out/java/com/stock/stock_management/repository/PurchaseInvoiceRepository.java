package com.stock.stock_management.repository;

import com.stock.stock_management.entity.PurchaseInvoice;

public interface PurchaseInvoiceRepository extends BaseRepository<PurchaseInvoice, Long> {

    long countByPurchaseOrderId(Long purchaseOrderId);

}
