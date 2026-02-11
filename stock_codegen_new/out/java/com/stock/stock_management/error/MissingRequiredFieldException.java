package com.stock.stock_management.error;

public class MissingRequiredFieldException extends RuntimeException {
    public MissingRequiredFieldException(String message) { super(message); }
}
