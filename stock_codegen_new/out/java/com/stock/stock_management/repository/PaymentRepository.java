package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Payment;

public interface PaymentRepository extends BaseRepository<Payment, Long> {

    long countBySalesOrderId(Long salesOrderId);

}
