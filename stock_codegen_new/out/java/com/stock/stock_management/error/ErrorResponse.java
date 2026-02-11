package com.stock.stock_management.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        Map<String, Object> details,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(String code, String message, Map<String, Object> details) {
        return new ErrorResponse(code, message, details, OffsetDateTime.now());
    }
}
