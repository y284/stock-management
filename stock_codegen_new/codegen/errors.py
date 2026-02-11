# codegen/errors.py
from pathlib import Path
from typing import List
from .utils import to_camel

# ====== Static (base) artifacts ======

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

ERROR_CODE_TPL = """package {pkg}.error;

/** Application-wide error codes (stable identifiers for clients). */
public enum ErrorCode {{
    // Generic
    NOT_FOUND,
    DUPLICATE,
    FK_NOT_FOUND,
    BAD_REQUEST,
    REF_INTEGRITY,
    VALIDATION_ERROR,
    DATA_INTEGRITY,
    INTERNAL_ERROR,

{per_table_codes}
}}
"""

CONSTRAINT_CATALOG_TPL = """package {pkg}.error;

import java.util.*;

public final class ConstraintCatalog {{
    private static final Map<String, ErrorCode> byConstraint = new HashMap<>();

    static {{
{entries}
    }}

    private ConstraintCatalog() {{}}

    /** @return Specific ErrorCode for a DB/DDL constraint name (case-insensitive), or empty if unknown. */
    public static Optional<ErrorCode> resolve(String constraintName) {{
        if (constraintName == null) return Optional.empty();
        String key = constraintName.toLowerCase(Locale.ROOT);
        ErrorCode code = byConstraint.get(key);
        return Optional.ofNullable(code);
    }}
}}
"""

# Updated handler uses ErrorCode + ConstraintCatalog
HANDLER_TPL = """package {pkg}.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {{
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND.name(), ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {{
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.DUPLICATE.name(), ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(ForeignKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFk(ForeignKeyNotFoundException ex) {{
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(ErrorCode.FK_NOT_FOUND.name(), ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ErrorResponse> handleMissing(MissingRequiredFieldException ex) {{
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.name(), ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidValueException ex) {{
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.name(), ex.getMessage(), Map.of()));
    }}

    @ExceptionHandler(ReferentialIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleRefIntegrity(ReferentialIntegrityException ex) {{
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.REF_INTEGRITY.name(), ex.getMessage(), Map.of()));
    }}

    // DTO @Valid binding errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(MethodArgumentNotValidException ex) {{
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details));
    }}

    // @Validated on params (@PathVariable/@RequestParam) errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {{
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> details.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details));
    }}

    // DB constraint safety net (unique/fk/not-null). Tries to resolve to a specific ErrorCode via ConstraintCatalog.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {{
        String constraint = extractConstraintName(ex);
        Optional<ErrorCode> code = ConstraintCatalog.resolve(constraint);
        ErrorCode ec = code.orElse(ErrorCode.DATA_INTEGRITY);
        HttpStatus status = (ec == ErrorCode.DUPLICATE) ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ec.name(), "Database constraint violated", Map.of("constraint", constraint)));
    }}

    // Last-resort catch to avoid leaking 500s
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {{
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR.name(), "Unexpected error", Map.of()));
    }}

    // Basic heuristic: many drivers include 'constraint [name]' or 'violates constraint "name"'
    private String extractConstraintName(Throwable t) {{
        if (t == null) return null;
        String msg = String.valueOf(t.getMessage());
        // naive patterns
        int i = msg.toLowerCase().indexOf("constraint");
        if (i >= 0) {{
            int q1 = msg.indexOf('"', i);
            int q2 = (q1 >= 0) ? msg.indexOf('"', q1 + 1) : -1;
            if (q1 >= 0 && q2 > q1) return msg.substring(q1 + 1, q2);
            int b1 = msg.indexOf('(', i);
            int b2 = (b1 >= 0) ? msg.indexOf(')', b1 + 1) : -1;
            if (b1 >= 0 && b2 > b1) return msg.substring(b1 + 1, b2);
        }}
        return extractConstraintName(t.getCause());
    }}
}}
"""

# ====== Generator that uses Table/Column to prebuild codes & constraint map ======

class ErrorGenerator:
    def __init__(self, out_dir: str, base_package: str):
        self.pkg = base_package
        self.out_dir = Path(out_dir) / Path(*base_package.split(".")) / "error"
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _emit(self, filename: str, content: str):
        (self.out_dir / filename).write_text(content, encoding="utf-8")

    def _emit_if_absent(self, filename: str, content: str):
        fp = self.out_dir / filename
        if not fp.exists():
            fp.write_text(content, encoding="utf-8")

    def _emit_exceptions(self):
        for name in EXCEPTIONS:
            self._emit_if_absent(f"{name}.java", EX_TPL.format(pkg=self.pkg, name=name))

    def _emit_error_response(self):
        self._emit_if_absent("ErrorResponse.java", ERROR_RESPONSE_TPL.format(pkg=self.pkg))

    def _emit_error_code(self, tables: List):
        per_table = []
        for t in tables:
            entity = to_camel(t.name)
            for c in t.columns:
                base = f"{entity}_{c.name}".upper().replace("-", "_")
                if getattr(c, "unique", None):
                    per_table.append(f"    {base}_DUPLICATE,")
                if getattr(c, "nullable", None) is False:
                    per_table.append(f"    {base}_REQUIRED,")
                if getattr(c, 'foreign_key_table', None):
                    per_table.append(f"    {base}_FK_VIOLATION,")
        # de-dup while preserving order
        seen = set(); dedup = []
        for x in per_table:
            if x not in seen:
                dedup.append(x); seen.add(x)
        content = ERROR_CODE_TPL.format(pkg=self.pkg, per_table_codes="\n".join(dedup))
        self._emit("ErrorCode.java", content)

    def _emit_constraint_catalog(self, tables: List):
        """
        Build a map of likely constraint names -> ErrorCode.
        Naming convention (adjust if your DDL differs):
          - unique: uk_<table>_<column>
          - foreign key: fk_<table>_<column>
          - not-null: nn_<table>_<column>
        Keys are stored lowercased for case-insensitive resolution.
        """
        lines = []
        for t in tables:
            entity = to_camel(t.name)
            for c in t.columns:
                col = c.name
                const_base = f"{entity}_{col}".upper().replace("-", "_")
                # UNIQUE
                if getattr(c, "unique", None):
                    lines.append(
                        f'        byConstraint.put("uk_{t.name}_{col}".toLowerCase(java.util.Locale.ROOT), '
                        f'ErrorCode.{const_base}_DUPLICATE);'
                    )               
                # FK
                if getattr(c, "foreign_key_table", None):
                    lines.append(
                        f'        byConstraint.put("fk_{t.name}_{col}".toLowerCase(java.util.Locale.ROOT), '
                        f'ErrorCode.{const_base}_FK_VIOLATION);'
                    )
                 # NOT NULL
                if getattr(c, "nullable", None) is False:
                    lines.append(
                        f'        byConstraint.put("nn_{t.name}_{col}".toLowerCase(java.util.Locale.ROOT), '
                        f'ErrorCode.{const_base}_REQUIRED);'
                    )
        content = CONSTRAINT_CATALOG_TPL.format(pkg=self.pkg, entries="\n".join(lines) if lines else "        // none")
        self._emit("ConstraintCatalog.java", content)

    def _emit_global_handler(self):
        self._emit("GlobalExceptionHandler.java", HANDLER_TPL.format(pkg=self.pkg))

    def generate(self, tables: List):
        self._emit_exceptions()
        self._emit_error_response()
        self._emit_error_code(tables)
        self._emit_constraint_catalog(tables)
        self._emit_global_handler()
