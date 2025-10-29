package com.stock.stock_management.error;

public class ForeignKeyNotFoundException extends RuntimeException {
    public ForeignKeyNotFoundException(String message) { super(message); }
}
