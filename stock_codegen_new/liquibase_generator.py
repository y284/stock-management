from typing import List, Tuple
import xml.etree.ElementTree as ET
from xml.dom import minidom
from pathlib import Path
from models import Table, Column

DBCHANGELOG_NS = "http://www.liquibase.org/xml/ns/dbchangelog"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"
SCHEMA_LOC = "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.31.xsd"

ET.register_namespace("", DBCHANGELOG_NS)
ET.register_namespace("xsi", XSI_NS)

def _is_computed_default(val: str) -> bool:
    if val is None:
        return False
    v = val.strip()
    keywords = ["CURRENT_TIMESTAMP", "CURRENT_DATE", "NOW()", "gen_random_uuid()", "uuid_generate_v4()"]
    return any(v.upper().startswith(k.split('(')[0]) for k in keywords) or v.endswith(")") or "::" in v

def _ensure_baseline_columns(t: Table) -> Table:
    from dataclasses import replace
    cols_by_name = {c.name: c for c in t.columns}
    has_pk = any(c.primary_key for c in t.columns)

    if not has_pk and "id" not in cols_by_name:
        cols_by_name["id"] = Column(name="id", type="BIGINT", primary_key=True)

    if "uuid" not in cols_by_name:
        cols_by_name["uuid"] = Column(
            name="uuid", type="UUID", default_value="gen_random_uuid()",
            unique=True, nullable=False
        )
    else:
        c = cols_by_name["uuid"]
        c.unique = True if c.unique is None else c.unique
        c.nullable = False if c.nullable is None else c.nullable
        if not c.default_value:
            c.default_value = "gen_random_uuid()"

    # created_at / updated_at as before
    if "created_at" not in cols_by_name:
        cols_by_name["created_at"] = Column(
            name="created_at", type="TIMESTAMPTZ",
            default_value="CURRENT_TIMESTAMP", nullable=False
        )
    else:
        if cols_by_name["created_at"].nullable is None:
            cols_by_name["created_at"].nullable = False

    if "updated_at" not in cols_by_name:
        cols_by_name["updated_at"] = Column(
            name="updated_at", type="TIMESTAMPTZ",
            default_value="CURRENT_TIMESTAMP", nullable=False
        )
    else:
        if cols_by_name["updated_at"].nullable is None:
            cols_by_name["updated_at"].nullable = False

    # >>> NEW: optimistic locking column
    if "version" not in cols_by_name:
        cols_by_name["version"] = Column(
            name="version", type="BIGINT", default_value="0", nullable=False
        )
    else:
        if cols_by_name["version"].nullable is None:
            cols_by_name["version"].nullable = False
        if cols_by_name["version"].default_value is None:
            cols_by_name["version"].default_value = "0"

    if "deleted" not in cols_by_name:
        cols_by_name["deleted"] = Column(
            name="deleted", type="BOOLEAN",
            default_value="false", nullable=False
        )
    else:
        if cols_by_name["deleted"].nullable is None:
            cols_by_name["deleted"].nullable = False
        if cols_by_name["deleted"].default_value is None:
            cols_by_name["deleted"].default_value = "false"

    if "deleted_at" not in cols_by_name:
        cols_by_name["deleted_at"] = Column(
            name="deleted_at", type="TIMESTAMPTZ", nullable=True
        )

    injected_order = [
        "id",
        "uuid",
        "created_at",
        "updated_at",
        "version",
        "deleted",
        "deleted_at"
    ]
    
    ordered = []
    for n in injected_order:
        if n in cols_by_name:
            ordered.append(cols_by_name.pop(n))
    seen = set(c.name for c in ordered)
    for c in t.columns:
        if c.name not in seen and c.name in cols_by_name:
            ordered.append(cols_by_name.pop(c.name))
    ordered.extend(cols_by_name.values())

    return replace(t, columns=ordered)

def _pretty(elem: ET.Element) -> str:
    rough = ET.tostring(elem, encoding="utf-8")
    return minidom.parseString(rough).toprettyxml(indent="  ", encoding="utf-8").decode("utf-8")

def build_table_changeset(t: Table, author: str, idx: int) -> ET.Element:
    t = _ensure_baseline_columns(t)

    root = ET.Element(ET.QName(DBCHANGELOG_NS, "databaseChangeLog"),
                      {ET.QName(XSI_NS, "schemaLocation"): SCHEMA_LOC})
    cs = ET.SubElement(root, ET.QName(DBCHANGELOG_NS, "changeSet"),
                       {"id": f"{idx:03d}-{t.name}-table", "author": author})

    attrs = {"tableName": t.name}
    if t.schema_name: attrs["schemaName"] = t.schema_name
    if t.remarks: attrs["remarks"] = t.remarks

    ct = ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "createTable"), attrs)
    for col in t.columns:
        c = ET.SubElement(ct, ET.QName(DBCHANGELOG_NS, "column"),
                        {"name": col.name, "type": col.type})

        # defaults
        if col.default_value is not None:
            if _is_computed_default(col.default_value):
                c.set("defaultValueComputed", col.default_value)
            else:
                c.set("defaultValue", col.default_value)

        # constraints block (nullable + primary key + unique)
        if (
            col.nullable is not None
            or col.primary_key
            or col.unique is True
        ):
            cons = ET.SubElement(c, ET.QName(DBCHANGELOG_NS, "constraints"))

            if col.nullable is not None:
                cons.set("nullable", "true" if col.nullable else "false")

            if col.primary_key:
                cons.set("primaryKey", "true")
                cons.set("primaryKeyName", f"pk_{t.name}")

            if col.unique and not col.primary_key:
                cons.set("unique", "true")

    return root


def build_constraints_changeset(t: Table, author: str, idx: int) -> ET.Element:
    t = _ensure_baseline_columns(t)

    root = ET.Element(ET.QName(DBCHANGELOG_NS, "databaseChangeLog"),
                      {ET.QName(XSI_NS, "schemaLocation"): SCHEMA_LOC})
    cs = ET.SubElement(root, ET.QName(DBCHANGELOG_NS, "changeSet"),
                       {"id": f"{idx:03d}-{t.name}-constraints", "author": author})

    # If 'id' is the only PK we know, add auto-increment for it (Postgres OK)
    injected_id_pk = any(c.name == "id" and c.primary_key for c in t.columns) and \
                     not any(c.primary_key and c.name != "id" for c in t.columns)

    if injected_id_pk:
        ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "addAutoIncrement"), {
            "tableName": t.name,
            "columnName": "id",
            "columnDataType": "BIGINT",
            "incrementBy": "1",
            "startWith": "1"
        })

    created_indexes = set()

    # --- UNIQUE constraints (skip UUID here; we'll create a unique index for it below) ---
    for col in t.columns:
        if (col.unique is True) and (not col.primary_key) and (col.name.lower() != "uuid"):
            ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "addUniqueConstraint"), {
                "tableName": t.name,
                "columnNames": col.name,
                "constraintName": f"uq_{t.name}_{col.name}"
            })

    # --- Always create a UNIQUE index on UUID (acts as uniqueness + index) ---
    # This avoids relying on DB-specific behavior and gives us a named, reusable index.
    uuid_idx_name = f"idx_{t.name}_uuid"
    uuid_col_exists = any(c.name.lower() == "uuid" for c in t.columns)
    if uuid_col_exists:
        idx_el = ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "createIndex"), {
            "tableName": t.name,
            "indexName": uuid_idx_name,
            "unique": "true"
        })
        ET.SubElement(idx_el, ET.QName(DBCHANGELOG_NS, "column"), {"name": "uuid"})
        created_indexes.add(uuid_idx_name)

    # --- Foreign keys + ensure index on FK columns (Postgres doesn’t auto-index FKs) ---
    for col in t.columns:
        if col.foreign_key_table and col.foreign_key_column:
            ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "addForeignKeyConstraint"), {
                "constraintName": f"fk_{t.name}_{col.name}_{col.foreign_key_table}",
                "baseTableName": t.name,
                "baseColumnNames": col.name,
                "referencedTableName": col.foreign_key_table,
                "referencedColumnNames": col.foreign_key_column
            })
            idx_name = f"idx_{t.name}_{col.name}"
            if idx_name not in created_indexes:
                idx_el = ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "createIndex"), {
                    "tableName": t.name,
                    "indexName": idx_name
                })
                ET.SubElement(idx_el, ET.QName(DBCHANGELOG_NS, "column"), {"name": col.name})
                created_indexes.add(idx_name)

    # --- Additional non-unique indexes (avoid duplicates; skip if already PK/unique) ---
    for col in t.columns:
        if getattr(col, "is_index", False) and not col.primary_key and not (col.unique is True):
            idx_name = f"idx_{t.name}_{col.name}"
            if idx_name not in created_indexes:
                idx_el = ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "createIndex"), {
                    "tableName": t.name,
                    "indexName": idx_name
                })
                ET.SubElement(idx_el, ET.QName(DBCHANGELOG_NS, "column"), {"name": col.name})
                created_indexes.add(idx_name)

    return root

def build_postgres_extensions_changeset(author: str) -> ET.Element:
    root = ET.Element(ET.QName(DBCHANGELOG_NS, "databaseChangeLog"),
                      {ET.QName(XSI_NS, "schemaLocation"): SCHEMA_LOC})
    cs = ET.SubElement(root, ET.QName(DBCHANGELOG_NS, "changeSet"),
                       {"id": "000-postgres-extensions", "author": author})
    # pgcrypto provides gen_random_uuid()
    ET.SubElement(cs, ET.QName(DBCHANGELOG_NS, "sql")).text = "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
    return root

def write_versioned_files(version_dir: Path, tables: List[Table], author: str = "mehdi") -> Tuple[list, list, Path | None]:
    version_dir.mkdir(parents=True, exist_ok=True)

    # 000 - extensions (optional)
    ext_file = version_dir / "000-postgres-extensions.xml"
    ext_xml = build_postgres_extensions_changeset(author)
    ext_file.write_text(_pretty(ext_xml), encoding="utf-8")

    table_files, constraint_files = [], []
    for i, t in enumerate(tables, start=1):
        table_xml = build_table_changeset(t, author, i)
        cons_xml  = build_constraints_changeset(t, author, i)
        table_file = version_dir / f"{i:03d}-{t.name}-table.xml"
        cons_file  = version_dir / f"{i:03d}-{t.name}-constraints.xml"
        table_file.write_text(_pretty(table_xml), encoding="utf-8")
        cons_file.write_text(_pretty(cons_xml), encoding="utf-8")
        table_files.append(table_file)
        constraint_files.append(cons_file)

    return table_files, constraint_files, ext_file

def write_master(master_file: Path, include_files: List[Path]):
    root = ET.Element(ET.QName(DBCHANGELOG_NS, "databaseChangeLog"),
                      {ET.QName(XSI_NS, "schemaLocation"): SCHEMA_LOC})

    # 1️⃣ Tables first
    for f in sorted(include_files, key=lambda p: ("-constraints" in p.name, p.name)):
        rel = f.relative_to(master_file.parent)
        ET.SubElement(root, ET.QName(DBCHANGELOG_NS, "include"), {
            "file": str(rel).replace("\\", "/"),
            "relativeToChangelogFile": "true"
        })

    master_file.parent.mkdir(parents=True, exist_ok=True)
    master_file.write_text(_pretty(root), encoding="utf-8")
