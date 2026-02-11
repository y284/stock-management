from pathlib import Path
from typing import Sequence, Optional, Dict, Any, List
from models import Table
from .utils import to_camel, to_lower_camel, java_type

BASE_REPO_TPL = """package {pkg}.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import {pkg}.entity.BaseEntity;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {{

    Optional<T> findByUuid(UUID uuid);
    boolean existsByUuid(UUID uuid);
    boolean existsByUuidAndIdNot(UUID uuid, ID id);

    /** Obtain a reference proxy without hitting the DB (throws on first access if missing). */
    default T getRef(ID id) {{
        return getReferenceById(id);
    }}
}}
"""

REPO_TPL = """package {pkg}.repository;

import {pkg}.entity.{Entity};
{extra_imports}
{repo_annotation}public interface {Entity}Repository extends BaseRepository<{Entity}, {IdType}> {{

{methods}
}}
"""

def _base_field_name_from_fk(col_name: str) -> str:
    return col_name[:-3] if col_name.endswith("_id") else col_name

def _param(var: str) -> str:
    return to_lower_camel(var)

class RepositoryGenerator:
    def __init__(self, out_dir: str, package: str, annotate_repository: bool = False,
                 exception_spec: Optional[Dict[str, Any]] = None, all_tables: Optional[Sequence[Table]] = None):
        self.out_dir = Path(out_dir) / Path(*package.split(".")) / "repository"
        self.package = package
        self.annotate_repository = annotate_repository
        self.exception_spec: Dict[str, Any] = exception_spec or {}
        self.all_tables: List[Table] = list(all_tables or [])
        self._tables_by_name = {t.name: t for t in self.all_tables}
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _emit_base(self):
        base_file = self.out_dir / "BaseRepository.java"
        # Overwrite to ensure latest helpers are present
        base_file.write_text(BASE_REPO_TPL.format(pkg=self.package), encoding="utf-8")

    def _pk_columns(self, t: Table):
        return [c for c in getattr(t, "columns", []) if getattr(c, "primary_key", False)]

    def _resolve_id_type(self, t: Table, entity_name: str) -> tuple[str, str]:
        pk_cols = self._pk_columns(t)
        if len(pk_cols) > 1:
            id_type = f"{entity_name}Id"
            extra_imports = f"import {self.package}.entity.{id_type};"
            return id_type, extra_imports
        if len(pk_cols) == 1:
            return java_type(pk_cols[0].type), ""
        return "Long", ""

    def _resolve_parent_id_type_and_import(self, parent_table_name: str) -> tuple[str, str]:
        pt = self._tables_by_name.get(parent_table_name)
        if not pt:
            return "Long", ""
        entity = to_camel(pt.name)
        id_type, extra_import = self._resolve_id_type(pt, entity)
        return id_type, extra_import

    def _unique_methods(self, t: Table, id_type: str) -> list[str]:
        """
        For unique fields (from exception_spec['unique_fields']), generate:
          - existsBy<Field>(..), existsBy<Field>AndIdNot(..)
          - Optional<{Entity}> findBy<Field>(..)  [and IgnoreCase for VARCHAR/CHAR]
          - If unique FK: methods use <AssocName>Id with the parent's ID type
        """
        methods: list[str] = []
        spec = self.exception_spec.get(t.name, {})
        unique_fields = spec.get("unique_fields", []) if spec else []

        fk_columns = {c.name: c for c in getattr(t, "columns", []) if getattr(c, "foreign_key_table", None)}

        for col_name in unique_fields:
            if col_name in fk_columns:
                assoc = _base_field_name_from_fk(col_name)
                parent_id_type, _imp = self._resolve_parent_id_type_and_import(fk_columns[col_name].foreign_key_table)
                param = _param(f"{assoc}Id")
                # exists
                methods.append(f"    boolean existsBy{to_camel(assoc)}Id({parent_id_type} {param});")
                methods.append(f"    boolean existsBy{to_camel(assoc)}IdAndIdNot({parent_id_type} {param}, {id_type} id);")
                # finder
                methods.append(f"    java.util.Optional<{to_camel(t.name)}> findBy{to_camel(assoc)}Id({parent_id_type} {param});")
            else:
                col = next(c for c in t.columns if c.name == col_name)
                ftype = java_type(col.type)
                camel = to_camel(col_name)
                p = _param(col_name)
                # exists
                methods.append(f"    boolean existsBy{camel}({ftype} {p});")
                methods.append(f"    boolean existsBy{camel}AndIdNot({ftype} {p}, {id_type} id);")
                # finder (+IgnoreCase for String)
                if ftype == "String":
                    methods.append(f"    java.util.Optional<{to_camel(t.name)}> findBy{camel}IgnoreCase(String {p});")
                else:
                    methods.append(f"    java.util.Optional<{to_camel(t.name)}> findBy{camel}({ftype} {p});")

        return methods

    def _fk_count_methods(self, t: Table) -> tuple[list[str], list[str]]:
        methods: list[str] = []
        imports: list[str] = []
        for c in getattr(t, "columns", []):
            parent = getattr(c, "foreign_key_table", None)
            if not parent:
                continue
            assoc = _base_field_name_from_fk(c.name)
            method_name = f"countBy{to_camel(assoc)}Id"
            parent_id_type, parent_import = self._resolve_parent_id_type_and_import(parent)
            param_name = _param(f"{assoc}Id")
            methods.append(f"    long {method_name}({parent_id_type} {param_name});")
            if parent_import:
                imports.append(parent_import)
        imports = list(dict.fromkeys(imports))
        return methods, imports

    def generate(self, tables: Sequence[Table]) -> None:
        self._emit_base()

        for t in tables:
            entity = to_camel(t.name)
            id_type, extra_imports = self._resolve_id_type(t, entity)

            unique_methods = self._unique_methods(t, id_type)
            fk_count_methods, parent_id_imports = self._fk_count_methods(t)

            methods_block = "\n".join(unique_methods + fk_count_methods)
            if methods_block:
                methods_block += "\n"

            imports = []
            if extra_imports:
                imports.append(extra_imports)
            imports.extend(parent_id_imports)
            extra_imports_block = ("\n".join(imports) + "\n") if imports else ""

            repo_file = self.out_dir / f"{entity}Repository.java"
            repo_annotation = ""
            if self.annotate_repository:
                repo_annotation = "import org.springframework.stereotype.Repository;\n\n@Repository\n"

            content = REPO_TPL.format(
                pkg=self.package,
                Entity=entity,
                IdType=id_type,
                extra_imports=extra_imports_block,
                repo_annotation=repo_annotation,
                methods=methods_block
            )

            if id_type == "Long" and not self._pk_columns(t):
                content = (
                    f"/* Auto-generated: No primary key detected for table '{t.name}'. "
                    f"Falling back to ID type Long. */\n" + content
                )

            repo_file.write_text(content, encoding="utf-8")
