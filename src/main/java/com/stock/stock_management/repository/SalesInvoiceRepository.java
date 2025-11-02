package com.stock.stock_management.repository;

import com.stock.stock_management.entity.SalesInvoice;

public interface SalesInvoiceRepository extends BaseRepository<SalesInvoice, Long> {

    long countBySalesOrderId(Long salesOrderId);

}
