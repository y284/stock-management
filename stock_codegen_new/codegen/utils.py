import re
from typing import Optional

# ===== Naming helpers =====

def to_camel(s: str) -> str:
    """snake_case → PascalCase"""
    return re.sub(r'(^|[_\-\s])(\w)', lambda m: m.group(2).upper(), s)


def to_lower_camel(s: str) -> str:
    """snake_case → lowerCamelCase"""
    camel = to_camel(s)
    return camel[0].lower() + camel[1:] if camel else camel


def to_snake(s: str) -> str:
    """camelCase → snake_case"""
    return re.sub(r'(?<!^)(?=[A-Z])', '_', s).lower()


# ===== Type conversion =====

def java_type(sql_type: str) -> str:
    """Map SQL type to Java type"""
    if not sql_type:
        return "String"
    t = sql_type.strip().upper()
    if t.startswith("BIGINT"):
        return "Long"
    if t.startswith(("INT", "INTEGER", "SMALLINT")):
        return "Integer"
    if t.startswith(("FLOAT", "DOUBLE PRECISION")):
        return "Double"
    if t.startswith(("NUMERIC", "DECIMAL", "REAL")):
        return "java.math.BigDecimal"
    if t.startswith(("BOOLEAN", "BOOL")):
        return "Boolean"
    if t.startswith("DATE"):
        return "java.time.LocalDate"
    if "TIMESTAMPTZ" in t or "TIMESTAMP" in t:
        return "java.time.OffsetDateTime" if "TZ" in t else "java.time.LocalDateTime"
    if t.startswith("TIME"):
        return "java.time.LocalTime"
    if t.startswith("UUID"):
        return "java.util.UUID"
    if t.startswith(("BYTEA", "BLOB")):
        return "byte[]"
    if "CHAR" in t or "TEXT" in t or "CLOB" in t:
        return "String"
    if "ENUM" in t or "JSON" in t:
        return "String"
    return "String"


# ===== SQL attribute extraction =====

def sql_type_attrs(sql: str) -> dict:
    """Extract precision/scale/length from SQL type."""
    if not sql:
        return {}
    s = sql.strip().upper()

    if m := re.match(r"^VARCHAR\((\d+)\)$", s):
        return {"length": int(m.group(1))}
    if m := re.match(r"^CHAR\((\d+)\)$", s):
        return {"length": int(m.group(1))}
    if m := re.match(r"^(NUMERIC|DECIMAL)\((\d+),\s*(\d+)\)$", s):
        return {"precision": int(m.group(2)), "scale": int(m.group(3))}
    if m := re.match(r"^(NUMERIC|DECIMAL)\((\d+)\)$", s):
        return {"precision": int(m.group(2))}
    return {}


# ===== JPA annotation formatting =====

def column_annotation(c) -> str:
    """Generate @Column with nullable, unique, and precision attributes."""
    attrs = sql_type_attrs(c.type)
    nullable_part = f", nullable = {str(c.nullable).lower()}" if c.nullable is not None else ""
    unique_part = f", unique = {str(c.unique).lower()}" if c.unique is not None else ""
    length_part = f", length = {attrs['length']}" if "length" in attrs else ""
    precision_part = f", precision = {attrs['precision']}" if "precision" in attrs else ""
    scale_part = f", scale = {attrs['scale']}" if "scale" in attrs else ""
    return f'    @Column(name = "{c.name}"{nullable_part}{unique_part}{length_part}{precision_part}{scale_part})\n'


# ===== Relationship helpers =====

def fk_field_name(col_name: str) -> str:
    """Convert FK column name (product_id) → field name (product)."""
    base = col_name[:-3] if col_name.endswith("_id") else col_name
    return to_lower_camel(base)


# ===== General helpers =====

def pk_generation_strategy(java_t: str) -> str:
    """Return GenerationType annotation for numeric PKs."""
    if java_t in ("Long", "Integer", "Short"):
        return "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"
    return ""


def table_indexes(t) -> list[str]:
    """Collect @Index definitions for table."""
    idx = [f'        @Index(name = "idx_{t.name}_uuid", columnList = "uuid")']
    for c in getattr(t, "columns", []):
        if getattr(c, "is_index", False):
            idx.append(f'        @Index(name = "idx_{t.name}_{c.name}", columnList = "{c.name}")')
    return idx


def escape_quotes(s: Optional[str]) -> str:
    """Escape double quotes for safe Java annotation usage."""
    return s.replace('"', '\\"') if s else s
