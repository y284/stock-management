# codegen/services.py
import os
from typing import Sequence, Tuple, List, Dict, Any
from models import Table
from .utils import to_camel, to_lower_camel, java_type

# ===== Interface template =====
IFACE_TPL = """package {pkg}.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import {pkg}.dto.{Entity}Dto;
{extra_imports}
public interface {Entity}Service {{

    {Entity}Dto create({Entity}Dto dto);

    {Entity}Dto update({IdType} id, {Entity}Dto dto);

    {Entity}Dto patch({IdType} id, {Entity}Dto dto);

    Optional<{Entity}Dto> findById({IdType} id);

    Optional<{Entity}Dto> findByUuid(UUID uuid);

    List<{Entity}Dto> findAll();

    Page<{Entity}Dto> findAll(Pageable pageable);

    List<{Entity}Dto> findAll(Sort sort);

    void deleteById({IdType} id);

    void deleteByUuid(UUID uuid);
}}
"""

# ===== Implementation template =====
IMPL_TPL = """package {pkg}.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import {pkg}.dto.{Entity}Dto;
import {pkg}.entity.{Entity};
{extra_imports}
{error_imports}
{parent_repo_imports}
{child_repo_imports}
import {pkg}.mapper.{Entity}Mapper;
import {pkg}.repository.{Entity}Repository;
import {pkg}.service.{Entity}Service;

@Service
@RequiredArgsConstructor
public class {Entity}ServiceImpl implements {Entity}Service {{

    private final {Entity}Repository repository;
    private final {Entity}Mapper mapper;
{extra_repo_fields}

    // ========= Create =========
    @Override
    @Transactional
    public {Entity}Dto create({Entity}Dto dto) {{
        precheckCreate(dto);
        {Entity} entity = mapper.toEntity(dto);{fk_bind_create}
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }}

    // ========= Update (full replace) =========
    @Override
    @Transactional
    public {Entity}Dto update({IdType} id, {Entity}Dto dto) {{
        // Load current (404 if missing)
        {Entity} current = repository.findById(id)
            .orElseThrow(() -> new {not_found_ex}("{EntityLower} not found with id=" + id));

        precheckUpdate(id, dto);

        // Build a replacement from DTO
        {Entity} replaced = mapper.toEntity(dto);

        // Enforce identifier (so Hibernate updates instead of inserting)
        {id_enforce}

        // Preserve immutable / audit when omitted in DTO
        if (replaced.getUuid() == null) {{ replaced.setUuid(current.getUuid()); }}
        if (replaced.getCreatedAt() == null) {{ replaced.setCreatedAt(current.getCreatedAt()); }}

        // ===== Version handling =====
        // If DTO carries version -> use it (optimistic locking by Hibernate).
        // Else preserve current version to avoid null-version issues.
        if (replaced.getVersion() == null) {{
            replaced.setVersion(current.getVersion());
        }}{fk_bind_update}

        replaced = repository.save(replaced);
        return mapper.toDto(replaced);
    }}

    // ========= Patch (partial update) =========
    @Override
    @Transactional
    public {Entity}Dto patch({IdType} id, {Entity}Dto dto) {{
        {Entity} entity = repository.findById(id)
            .orElseThrow(() -> new {not_found_ex}("{EntityLower} not found with id=" + id));

        precheckUpdate(id, dto);
        // Non-null fields only (per mapper config)
        mapper.updateEntityFromDto(dto, entity);{fk_bind_patch}
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }}

    // ========= Queries =========
    @Override
    @Transactional(readOnly = true)
    public Optional<{Entity}Dto> findById({IdType} id) {{
        return repository.findById(id).map(mapper::toDto);
    }}

    @Override
    @Transactional(readOnly = true)
    public Optional<{Entity}Dto> findByUuid(UUID uuid) {{
        return repository.findByUuid(uuid).map(mapper::toDto);
    }}

    @Override
    @Transactional(readOnly = true)
    public List<{Entity}Dto> findAll() {{
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }}

    @Override
    @Transactional(readOnly = true)
    public Page<{Entity}Dto> findAll(Pageable pageable) {{
        return repository.findAll(pageable).map(mapper::toDto);
    }}

    @Override
    @Transactional(readOnly = true)
    public List<{Entity}Dto> findAll(Sort sort) {{
        return repository.findAll(sort).stream().map(mapper::toDto).collect(Collectors.toList());
    }}

    // ========= Delete =========
    @Override
    @Transactional
    public void deleteById({IdType} id) {{
        if (!repository.existsById(id)) {{
            throw new {not_found_ex}("{EntityLower} not found with id=" + id);
        }}
        guardDelete(id);
        repository.deleteById(id);
    }}

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {{
        repository.findByUuid(uuid).ifPresent(entity -> {{
            guardDelete(entity.getId());
            repository.delete(entity);
        }});
    }}

    // ========= Prechecks derived from schema/spec =========
{precheck_block}

    // ========= Delete guard (child refs) =========
{guard_block}
}}
"""

def _pk_columns(t: Table) -> List:
    return [c for c in t.columns if getattr(c, "primary_key", False)]

def _has_single_pk(t: Table) -> bool:
    return len(_pk_columns(t)) == 1

def _resolve_id(t: Table, entity_name: str) -> Tuple[str, str, bool]:
    """
    Returns (IdType, id_setter, is_composite)
      - composite: (EntityId, 'setId', True)
      - single: (javaType, 'set<ToCamel(pk_field)>', False)
      - none: (Long, 'setId', False)  # fallback
    """
    pk_cols = _pk_columns(t)
    if len(pk_cols) > 1:
        return f"{entity_name}Id", "setId", True
    if len(pk_cols) == 1:
        pk = pk_cols[0]
        pk_java = java_type(pk.type)
        setter = f"set{to_camel(pk.name)}"
        return pk_java, setter, False
    return "Long", "setId", False

def _getter(col_name: str) -> str:
    return f"get{to_camel(col_name)}"

def _assoc_from_fk(col_name: str) -> str:
    return col_name[:-3] if col_name.endswith("_id") else col_name

class ServiceGenerator:
    def __init__(self, out_dir: str, package: str,
                 exception_spec: Dict[str, Any] | None = None,
                 all_tables: Sequence[Table] | None = None):
        self.out_dir = out_dir
        self.package = package
        self.exception_spec = exception_spec or {}
        self.all_tables: List[Table] = list(all_tables or [])
        self._tables_by_name = {t.name: t for t in self.all_tables}

    def _resolve_parent_id_type(self, parent_table: str, parent_entity: str) -> Tuple[str, str, bool]:
        """
        Returns (parent_id_type, extra_import_if_composite, parent_has_single_pk)
        """
        pt = self._tables_by_name.get(parent_table)
        if not pt:
            return "Long", "", True
        id_type, _setter, is_composite = _resolve_id(pt, parent_entity)
        extra_import = f"import {self.package}.entity.{parent_entity}Id;" if is_composite else ""
        return id_type, extra_import, not is_composite

    def generate(self, tables: Sequence[Table]) -> None:
        base_dir = os.path.join(self.out_dir, *self.package.split("."), "service")
        impl_base = os.path.join(base_dir, "impl")
        os.makedirs(base_dir, exist_ok=True)
        os.makedirs(impl_base, exist_ok=True)

        for t in tables:
            entity = to_camel(t.name)
            id_type, id_setter, is_composite = _resolve_id(t, entity)
            entity_lower = entity[0].lower() + entity[1:]

            # ===== Imports =====
            extra_iface_imports = ""
            extra_impl_imports: List[str] = []
            if is_composite:
                entity_id_fqn = f"{self.package}.entity.{entity}Id"
                extra_iface_imports = f"import {entity_id_fqn};"
                extra_impl_imports.append(f"import {entity_id_fqn};")

            error_imports = "\n".join([
                f"import {self.package}.error.ResourceNotFoundException;",
                f"import {self.package}.error.DuplicateResourceException;",
                f"import {self.package}.error.ForeignKeyNotFoundException;",
                f"import {self.package}.error.MissingRequiredFieldException;",
                f"import {self.package}.error.ReferentialIntegrityException;"
            ])

            # ===== Parent repositories (FKs) — deduped =====
            fk_columns = [c for c in t.columns if getattr(c, "foreign_key_table", None)]
            parent_repo_imports: List[str] = []
            parent_repo_fields: List[str] = []
            parent_repo_names: Dict[str, str] = {}
            parent_single_pk: Dict[str, bool] = {}
            parents_seen: set[str] = set()

            for c in fk_columns:
                parent_table = c.foreign_key_table
                parent_entity = to_camel(parent_table)

                if parent_table not in parents_seen:
                    parent_repo_imports.append(f"import {self.package}.repository.{parent_entity}Repository;")
                    field_name = f"{parent_entity[0].lower() + parent_entity[1:]}Repository"
                    parent_repo_fields.append(f"    private final {parent_entity}Repository {field_name};")
                    parent_repo_names[parent_table] = field_name
                    parents_seen.add(parent_table)

                pid_type, pid_import, is_single = self._resolve_parent_id_type(parent_table, parent_entity)
                parent_single_pk[parent_table] = is_single
                if pid_import:
                    extra_impl_imports.append(pid_import)

            # ===== Child refs for delete guard =====
            spec = self.exception_spec.get(t.name, {})
            child_refs = spec.get("child_refs", []) if spec else []
            child_repo_imports: List[str] = []
            child_repo_fields: List[str] = []
            child_repo_calls: List[str] = []

            for ref in child_refs:
                child_table = ref["child_table"]
                child_fk = ref["child_fk"]
                child_entity = to_camel(child_table)
                child_repo_imports.append(f"import {self.package}.repository.{child_entity}Repository;")
                child_field = f"{child_entity[0].lower() + child_entity[1:]}Repository"
                if f"    private final {child_entity}Repository {child_field};" not in child_repo_fields:
                    child_repo_fields.append(f"    private final {child_entity}Repository {child_field};")

                assoc = _assoc_from_fk(child_fk)
                method = f"countBy{to_camel(assoc)}Id"
                child_repo_calls.append(
                    f'if ({child_field}.{method}(id) > 0) {{ throw new ReferentialIntegrityException("{entity_lower} has dependent {child_table} records"); }}'
                )

            # ===== Prechecks =====
            unique_fields: List[str] = spec.get("unique_fields", []) if spec else []
            required_fields: List[str] = spec.get("required_fields", []) if spec else []

            precheck_lines_create: List[str] = []
            precheck_lines_update: List[str] = []

            # Required fields
            for col in required_fields:
                getter = _getter(col)
                precheck_lines_create.append(
                    f'if (dto.{getter}() == null) {{ throw new MissingRequiredFieldException("{col} is required"); }}'
                )
                precheck_lines_update.append(
                    f'if (dto.{getter}() == null) {{ throw new MissingRequiredFieldException("{col} is required"); }}'
                )

            # Unique fields
            fk_colnames = {c.name for c in fk_columns}
            for col in unique_fields:
                if col in fk_colnames:
                    parent_table = next(c.foreign_key_table for c in fk_columns if c.name == col)
                    if not parent_single_pk.get(parent_table, True):
                        continue
                    assoc = _assoc_from_fk(col)
                    getter = _getter(f"{assoc}_id")
                    exists_m = f"existsBy{to_camel(assoc)}Id"
                    exists_not_m = f"{exists_m}AndIdNot"
                    precheck_lines_create.append(
                        f'if (dto.{getter}() != null && repository.{exists_m}(dto.{getter}())) {{ '
                        f'throw new DuplicateResourceException("{entity_lower} with {col} already exists"); }}'
                    )
                    precheck_lines_update.append(
                        f'if (dto.{getter}() != null && repository.{exists_not_m}(dto.{getter}(), id)) {{ '
                        f'throw new DuplicateResourceException("{entity_lower} with {col} already exists"); }}'
                    )
                else:
                    getter = _getter(col)
                    exists_m = f"existsBy{to_camel(col)}"
                    exists_not_m = f"{exists_m}AndIdNot"
                    precheck_lines_create.append(
                        f'if (dto.{getter}() != null && repository.{exists_m}(dto.{getter}())) {{ '
                        f'throw new DuplicateResourceException("{entity_lower} with {col} already exists"); }}'
                    )
                    precheck_lines_update.append(
                        f'if (dto.{getter}() != null && repository.{exists_not_m}(dto.{getter}(), id)) {{ '
                        f'throw new DuplicateResourceException("{entity_lower} with {col} already exists"); }}'
                    )

            # FK existence checks
            has_composite_pk = len(_pk_columns(t)) > 1

            for c in fk_columns:
                parent_table = c.foreign_key_table
                is_parent_single = parent_single_pk.get(parent_table, True)
                assoc = _assoc_from_fk(c.name)

                if has_composite_pk and getattr(c, "primary_key", False):
                    parent_repo_field = parent_repo_names[parent_table]
                    precheck_lines_create.append(
                        f'if (dto.getId() != null && dto.getId().{_getter(c.name)}() != null && !{parent_repo_field}.existsById(dto.getId().{_getter(c.name)}())) {{ '
                        f'throw new ForeignKeyNotFoundException("{c.name} references missing {parent_table}"); }}'
                    )
                    precheck_lines_update.append(
                        f'if (!{parent_repo_field}.existsById(id.{_getter(c.name)}())) {{ '
                        f'throw new ForeignKeyNotFoundException("{c.name} references missing {parent_table}"); }}'
                    )
                    continue

                if not is_parent_single:
                    continue

                getter = _getter(f"{assoc}_id")
                parent_repo_field = parent_repo_names[parent_table]
                precheck_lines_create.append(
                    f'if (dto.{getter}() != null && !{parent_repo_field}.existsById(dto.{getter}())) {{ '
                    f'throw new ForeignKeyNotFoundException("{c.name} references missing {parent_table}"); }}'
                )
                precheck_lines_update.append(
                    f'if (dto.{getter}() != null && !{parent_repo_field}.existsById(dto.{getter}())) {{ '
                    f'throw new ForeignKeyNotFoundException("{c.name} references missing {parent_table}"); }}'
                )

            precheck_block = []
            precheck_block.append(f"    private void precheckCreate({entity}Dto dto) {{")
            precheck_block.extend(["        " + ln for ln in precheck_lines_create] or ["        // no prechecks"])
            precheck_block.append("    }")
            precheck_block.append("")
            precheck_block.append(f"    private void precheckUpdate({id_type} id, {entity}Dto dto) {{")
            precheck_block.extend(["        " + ln for ln in precheck_lines_update] or ["        // no prechecks"])
            precheck_block.append("    }")
            precheck_block_src = "\n".join(precheck_block)

            # ===== FK binding (attach parents via JPA refs) =====
            bind_create_lines: List[str] = []
            bind_update_lines: List[str] = []
            bind_patch_lines:  List[str] = []

            has_composite_pk = len(_pk_columns(t)) > 1  # already computed earlier

            for c in fk_columns:
                parent_table = c.foreign_key_table
                assoc = _assoc_from_fk(c.name)
                parent_repo_field = parent_repo_names[parent_table]
                is_parent_single = parent_single_pk.get(parent_table, True)

                # if parent has composite PK, DTO doesn't expose <assoc>Id scalar => skip binding
                if not is_parent_single:
                    continue

                setter = f"set{to_camel(assoc)}"
                getter_id_scalar = _getter(f"{assoc}_id")         # e.g. dto.getProductId()
                getter_id_in_composite = _getter(c.name)          # e.g. getProductId() inside dto.getId()

                if has_composite_pk and getattr(c, "primary_key", False):
                    # ---- FK is part of the composite PK (MapsId case) ----
                    # CREATE: bind using dto.id.<fkPart> if present
                    bind_create_lines.append(
                        f"if (dto.getId() != null && dto.getId().{getter_id_in_composite}() != null) {{ "
                        f"entity.{setter}({parent_repo_field}.getRef(dto.getId().{getter_id_in_composite}())); }}"
                    )

                    # UPDATE: bind using method parameter 'id' (always present), not the DTO
                    bind_update_lines.append(
                        f"replaced.{setter}({parent_repo_field}.getRef(id.{getter_id_in_composite}()));"
                    )

                    # PATCH: bind only if user provided dto.id.<fkPart> (don’t override otherwise)
                    bind_patch_lines.append(
                        f"if (dto.getId() != null && dto.getId().{getter_id_in_composite}() != null) {{ "
                        f"entity.{setter}({parent_repo_field}.getRef(dto.getId().{getter_id_in_composite}())); }}"
                    )
                else:
                    # ---- Regular FK (not part of PK) ----
                    # CREATE: dto.<assoc>Id if present
                    bind_create_lines.append(
                        f"if (dto.{getter_id_scalar}() != null) {{ entity.{setter}({parent_repo_field}.getRef(dto.{getter_id_scalar}())); }}"
                    )
                    # UPDATE: dto.<assoc>Id if present
                    bind_update_lines.append(
                        f"if (dto.{getter_id_scalar}() != null) {{ replaced.{setter}({parent_repo_field}.getRef(dto.{getter_id_scalar}())); }}"
                    )
                    # PATCH: dto.<assoc>Id if present
                    bind_patch_lines.append(
                        f"if (dto.{getter_id_scalar}() != null) {{ entity.{setter}({parent_repo_field}.getRef(dto.{getter_id_scalar}())); }}"
                    )

            fk_bind_create_src = ("\n        " + "\n        ".join(bind_create_lines)) if bind_create_lines else ""
            fk_bind_update_src = ("\n        " + "\n        ".join(bind_update_lines)) if bind_update_lines else ""
            fk_bind_patch_src  = ("\n        " + "\n        ".join(bind_patch_lines))  if bind_patch_lines  else ""
            # ===== Guard delete block =====
            guard_lines = []
            if child_repo_calls:
                guard_lines.append(f"    private void guardDelete({id_type} id) {{")
                guard_lines.extend(["        " + ln for ln in child_repo_calls])
                guard_lines.append("    }")
            else:
                guard_lines.append(f"    private void guardDelete({id_type} id) {{ /* no children */ }}")
            guard_block_src = "\n".join(guard_lines)

            # ===== Imports/fields =====
            extra_impl_imports_src = "\n".join(sorted(set(extra_impl_imports))) if extra_impl_imports else ""
            parent_repo_imports_src = "\n".join(sorted(set(parent_repo_imports))) if parent_repo_imports else ""
            child_repo_imports_src = "\n".join(sorted(set(child_repo_imports))) if child_repo_imports else ""
            extra_repo_fields_src = ""
            if parent_repo_fields or child_repo_fields:
                extra_repo_fields_src = "\n" + "\n".join(parent_repo_fields + child_repo_fields)

            # ===== Write interface =====
            os.makedirs(base_dir, exist_ok=True)
            with open(os.path.join(base_dir, f"{entity}Service.java"), "w", encoding="utf-8") as f:
                f.write(IFACE_TPL.format(
                    pkg=self.package,
                    Entity=entity,
                    IdType=id_type,
                    extra_imports=(extra_iface_imports + "\n") if extra_iface_imports else ""
                ))

            # ===== Enforce id setter for update =====
            id_enforce = "replaced.setId(id);" if is_composite else f"replaced.{id_setter}(id);"

            # ===== Write implementation =====
            os.makedirs(impl_base, exist_ok=True)
            with open(os.path.join(impl_base, f"{entity}ServiceImpl.java"), "w", encoding="utf-8") as f:
                f.write(IMPL_TPL.format(
                    pkg=self.package,
                    Entity=entity,
                    EntityLower=entity_lower,
                    IdType=id_type,
                    id_enforce=id_enforce,
                    error_imports=error_imports,
                    not_found_ex="ResourceNotFoundException",
                    extra_imports=(extra_impl_imports_src + "\n") if extra_impl_imports_src else "",
                    parent_repo_imports=parent_repo_imports_src,
                    child_repo_imports=child_repo_imports_src,
                    extra_repo_fields=extra_repo_fields_src,
                    precheck_block=precheck_block_src,
                    guard_block=guard_block_src,
                    fk_bind_create=fk_bind_create_src,
                    fk_bind_update=fk_bind_update_src,
                    fk_bind_patch=fk_bind_patch_src
                ))
