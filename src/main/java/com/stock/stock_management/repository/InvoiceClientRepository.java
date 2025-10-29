package com.stock.stock_management.repository;

import com.stock.stock_management.entity.InvoiceClient;

public interface InvoiceClientRepository extends BaseRepository<InvoiceClient, Long> {

    long countBySaleCommandeId(Long saleCommandeId);

}
