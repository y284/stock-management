# codegen/errors.py
from pathlib import Path

EXCEPTIONS = [
    "ResourceNotFoundException",
    "DuplicateResourceException",
    "ForeignKeyNotFoundException",
    "MissingRequiredFieldException",
    "InvalidValueException",
    "ReferentialIntegrityException",
]

EX_TPL = """package {pkg}.error;

public class {name} extends RuntimeException {{
    public {name}(String message) {{ super(message); }}
}}
"""

ERROR_RESPONSE_TPL = """package {pkg}.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        Map<String, Object> details,
        OffsetDateTime timestamp
) {{
    public static ErrorResponse of(String code, String message, Map<String, Object> details) {{
        return new ErrorResponse(code, message, details, OffsetDateTime.now());
    }}
}}
"""

HANDLER_TPL = """package {pkg}.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {{
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND", ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {{
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE", ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(ForeignKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFk(ForeignKeyNotFoundException ex) {{
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of("FK_NOT_FOUND", ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ErrorResponse> handleMissing(MissingRequiredFieldException ex) {{
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("BAD_REQUEST", ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidValueException ex) {{
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("BAD_REQUEST", ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(ReferentialIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleRefIntegrity(ReferentialIntegrityException ex) {{
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("REF_INTEGRITY", ex.getMessage(), Map.of()));
    }}

    // DTO @Valid binding errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(MethodArgumentNotValidException ex) {{
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Validation failed", details));
    }}

    // @Validated on params (@PathVariable/@RequestParam) errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {{
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> details.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Validation failed", details));
    }}

    // DB constraint safety net (unique/fk/etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {{
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DATA_INTEGRITY", "Database constraint violated", Map.of()));
    }}

    // Last-resort catch to avoid leaking 500s
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {{
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected error", Map.of()));
    }}
}}
"""

class ErrorGenerator:
    def __init__(self, out_dir: str, base_package: str):
        self.pkg = base_package
        self.out_dir = Path(out_dir) / Path(*base_package.split(".")) / "error"
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _emit_if_absent(self, filename: str, content: str):
        fp = self.out_dir / filename
        if not fp.exists():
            fp.write_text(content, encoding="utf-8")

    def _emit_exceptions(self):
        for name in EXCEPTIONS:
            self._emit_if_absent(f"{name}.java", EX_TPL.format(pkg=self.pkg, name=name))

    def _emit_error_response(self):
        self._emit_if_absent("ErrorResponse.java", ERROR_RESPONSE_TPL.format(pkg=self.pkg))

    def _emit_global_handler(self):
        self._emit_if_absent("GlobalExceptionHandler.java", HANDLER_TPL.format(pkg=self.pkg))

    def generate(self):
        self._emit_exceptions()
        self._emit_error_response()
        self._emit_global_handler()


