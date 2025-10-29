package com.stock.stock_management.repository;

import com.stock.stock_management.entity.InvoiceSupplier;

public interface InvoiceSupplierRepository extends BaseRepository<InvoiceSupplier, Long> {

    long countBySupplierCommandeId(Long supplierCommandeId);

}
