package com.stock.stock_management.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND.name(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.DUPLICATE.name(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(ForeignKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFk(ForeignKeyNotFoundException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(ErrorCode.FK_NOT_FOUND.name(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ErrorResponse> handleMissing(MissingRequiredFieldException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.name(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidValueException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.name(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(ReferentialIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleRefIntegrity(ReferentialIntegrityException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.REF_INTEGRITY.name(), ex.getMessage(), Map.of()));
    }

    // DTO @Valid binding errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details));
    }

    // @Validated on params (@PathVariable/@RequestParam) errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> details.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details));
    }

    // DB constraint safety net (unique/fk/not-null). Tries to resolve to a specific ErrorCode via ConstraintCatalog.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String constraint = extractConstraintName(ex);
        Optional<ErrorCode> code = ConstraintCatalog.resolve(constraint);
        ErrorCode ec = code.orElse(ErrorCode.DATA_INTEGRITY);
        HttpStatus status = (ec == ErrorCode.DUPLICATE) ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ec.name(), "Database constraint violated", Map.of("constraint", constraint)));
    }

    // Last-resort catch to avoid leaking 500s
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR.name(), "Unexpected error", Map.of()));
    }

    // Basic heuristic: many drivers include 'constraint [name]' or 'violates constraint "name"'
    private String extractConstraintName(Throwable t) {
        if (t == null) return null;
        String msg = String.valueOf(t.getMessage());
        // naive patterns
        int i = msg.toLowerCase().indexOf("constraint");
        if (i >= 0) {
            int q1 = msg.indexOf('"', i);
            int q2 = (q1 >= 0) ? msg.indexOf('"', q1 + 1) : -1;
            if (q1 >= 0 && q2 > q1) return msg.substring(q1 + 1, q2);
            int b1 = msg.indexOf('(', i);
            int b2 = (b1 >= 0) ? msg.indexOf(')', b1 + 1) : -1;
            if (b1 >= 0 && b2 > b1) return msg.substring(b1 + 1, b2);
        }
        return extractConstraintName(t.getCause());
    }
}
