from pathlib import Path
from typing import List
import re
from .utils import to_camel, to_lower_camel, java_type, sql_type_attrs

# Base fields already present in BaseDto
BASE_COLUMNS = {"uuid", "created_at", "updated_at", "version"}

BASE_DTO_TPL = """package {pkg}.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.UUID;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class BaseDto {{
    private UUID uuid;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;
}}
"""

DTO_ID_TPL = """package {pkg}.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class {id_name} implements Serializable {{

{id_fields}
}}
"""

DTO_TPL = """package {pkg}.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class {name}Dto extends BaseDto {{

{fields}
}}
"""

FIELD_TPL = "    {annotations}private {type} {name};\n"


# ===== Validation utilities =====

def _looks_email(name: str) -> bool:
    return "email" in name.lower()


def _looks_non_negative(name: str) -> bool:
    n = name.lower()
    return any(k in n for k in ["qty", "quantity", "price", "total", "amount", "tva", "tax", "stock"])


def _validation_annotations(c, field_name: str) -> str:
    """Generate Jakarta Bean Validation annotations based on column metadata."""
    anns: list[str] = []

    # @NotNull if explicitly non-nullable
    if getattr(c, "nullable", None) is False:
        anns.append("@NotNull ")

    attrs = sql_type_attrs(getattr(c, "type", "") or "")

    # Strings
    if "length" in attrs:
        anns.append(f"@Size(max = {attrs['length']}) ")

    # Numbers
    if "precision" in attrs:
        p = attrs["precision"]
        s = attrs.get("scale", 0) or 0
        integer = max(p - s, 0)
        anns.append(f"@Digits(integer = {integer}, fraction = {s}) ")
        if _looks_non_negative(field_name):
            anns.append("@PositiveOrZero ")

    # Email heuristic
    if _looks_email(field_name):
        anns.append("@Email ")

    return "".join(anns)


# ===== DTO Generator =====

class DtoGenerator:
    def __init__(self, out_dir: str, base_package: str):
        self.out_dir = Path(out_dir) / Path(*base_package.split(".")) / "dto"
        self.pkg = base_package
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _emit_base_dto(self):
        base_file = self.out_dir / "BaseDto.java"
        if not base_file.exists():
            base_file.write_text(BASE_DTO_TPL.format(pkg=self.pkg), encoding="utf-8")

    def _emit_id_dto(self, t, pk_cols):
        """Generate {Entity}IdDto for composite keys."""
        id_name = f"{to_camel(t.name)}IdDto"
        id_fields_src = []
        for c in pk_cols:
            anns = _validation_annotations(c, to_lower_camel(c.name))
            id_fields_src.append(FIELD_TPL.format(annotations=anns, type=java_type(c.type), name=to_lower_camel(c.name)))
        content = DTO_ID_TPL.format(pkg=self.pkg, id_name=id_name, id_fields="".join(id_fields_src))
        (self.out_dir / f"{id_name}.java").write_text(content, encoding="utf-8")
        return id_name

    def _emit_dto(self, t):
        dto_name = to_camel(t.name)
        fields_src = []

        pk_cols = [c for c in t.columns if getattr(c, "primary_key", False)]
        pk_names = {c.name for c in pk_cols}
        has_composite_pk = len(pk_cols) > 1

        # 1. Primary key(s)
        if has_composite_pk:
            id_dto_name = self._emit_id_dto(t, pk_cols)
            fields_src.append(FIELD_TPL.format(annotations="", type=id_dto_name, name="id"))
        elif pk_cols:
            c = pk_cols[0]
            anns = _validation_annotations(c, to_lower_camel(c.name))
            fields_src.append(FIELD_TPL.format(annotations=anns, type=java_type(c.type), name=to_lower_camel(c.name)))

        # 2. Remaining fields
        for c in t.columns:
            if c.name in BASE_COLUMNS or c.name in pk_names:
                continue
            fname = to_lower_camel(c.name)
            anns = _validation_annotations(c, fname)
            fields_src.append(FIELD_TPL.format(annotations=anns, type=java_type(c.type), name=fname))

        # Deterministic ordering: PKs first, then others
        content = DTO_TPL.format(pkg=self.pkg, name=dto_name, fields="".join(fields_src))
        (self.out_dir / f"{dto_name}Dto.java").write_text(content, encoding="utf-8")

    def generate(self, tables: List):
        self._emit_base_dto()
        for t in tables:
            self._emit_dto(t)
