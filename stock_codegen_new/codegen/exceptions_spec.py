# codegen/exceptions_spec.py
from collections import defaultdict
from dataclasses import asdict

def derive_exception_specs(tables):
    """
    Build a per-table spec to drive exception-aware generation.

    Returns: dict[str, dict] like:
      {
        "product": {
          "required_fields": ["sku", "name"],
          "unique_fields": ["sku"],
          "foreign_keys": [
            {"column": "category_id", "ref_table": "category", "ref_column": "id"}
          ],
          "child_refs": [
            {"child_table": "stock_level", "child_fk": "product_id"},
            ...
          ]
        },
        ...
      }
    """
    # children map: parent_table -> list[(child_table, fk_column)]
    children = defaultdict(list)
    for t in tables:
        for c in t.columns:
            if getattr(c, "foreign_key_table", None):
                children[c.foreign_key_table].append((t.name, c.name))

    specs = {}
    for t in tables:
        required = []
        uniques = []
        fks = []

        for c in t.columns:
            # required fields: non-nullable, no default, and not PK
            if (c.nullable is False) and (not c.primary_key) and (c.default_value is None):
                required.append(c.name)

            if getattr(c, "unique", False):
                uniques.append(c.name)

            if getattr(c, "foreign_key_table", None):
                fks.append({
                    "column": c.name,
                    "ref_table": c.foreign_key_table,
                    "ref_column": c.foreign_key_column
                })

        specs[t.name] = {
            "required_fields": required,
            "unique_fields": uniques,
            "foreign_keys": fks,
            "child_refs": [
                {"child_table": ct, "child_fk": fk} for (ct, fk) in children.get(t.name, [])
            ],
        }

    return specs
