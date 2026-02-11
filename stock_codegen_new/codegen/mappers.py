# codegen/mappers.py
from pathlib import Path
from typing import List, Dict, Tuple, Set
from .utils import to_camel, to_lower_camel, java_type

BASE_MAPPER_CONFIG_TPL = """package {pkg};

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BaseMapperConfig {{}}
"""

BASE_AUDIT_MAPPER_TPL = """package {pkg};

import org.mapstruct.*;
import {base}.entity.BaseEntity;
import {base}.dto.BaseDto;

@Mapper(config = BaseMapperConfig.class)
public interface BaseAuditMapper {{

    BaseDto toDto(BaseEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({{
        @Mapping(target = "uuid", source = "uuid"),
        @Mapping(target = "createdAt", source = "createdAt"),
        @Mapping(target = "updatedAt", source = "updatedAt"),
        @Mapping(target = "version", source = "version")
    }})
    void copyAuditToDto(BaseEntity entity, @MappingTarget BaseDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({{
        @Mapping(target = "uuid", source = "uuid"),
        @Mapping(target = "createdAt", source = "createdAt"),
        @Mapping(target = "updatedAt", source = "updatedAt"),
        @Mapping(target = "version", source = "version")
    }})
    void updateAuditFromDto(BaseDto dto, @MappingTarget BaseEntity entity);
}}
"""

MAPPER_TPL = """package {pkg};

{imports}
import org.mapstruct.*;
import java.util.*;
import {base}.entity.{Entity};
import {base}.dto.{Entity}Dto;

@Mapper(config = BaseMapperConfig.class)
public interface {Entity}Mapper extends BaseAuditMapper {{

{to_entity_ann}    {Entity} toEntity({Entity}Dto dto);

{to_dto_ann}    {Entity}Dto toDto({Entity} entity);

    List<{Entity}Dto> toDtoList(List<{Entity}> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto({Entity}Dto dto, @MappingTarget {Entity} entity);
}}
"""

def _relation_base(col_name: str) -> str:
    base = col_name[:-3] if col_name.endswith("_id") else col_name
    return to_lower_camel(base)

def _build_target_pk_index(tables) -> Dict[str, Tuple[str, str, str]]:
    """
    For each target table (referenced by FKs), capture:
      - pk field name in the ENTITY (lowerCamel)
      - pk java type
      - setter name for that pk
    Only if the target has a single-column PK.
    """
    idx: Dict[str, Tuple[str, str, str]] = {}
    for t in tables:
        pk_cols = [c for c in t.columns if getattr(c, "primary_key", False)]
        if len(pk_cols) == 1:
            c = pk_cols[0]
            pk_field_lc = to_lower_camel(c.name)
            pk_java = java_type(c.type)
            setter = f"set{pk_field_lc[0].upper()}{pk_field_lc[1:]}"
            idx[t.name] = (pk_field_lc, pk_java, setter)
    return idx

class MapperGenerator:
    def __init__(self, out_dir: str, base_package: str):
        self.root_dir = Path(out_dir) / Path(*base_package.split("."))
        self.out_dir = self.root_dir / "mapper"
        self.pkg = base_package + ".mapper"
        self.base = base_package
        self.entity_pkg = base_package + ".entity"
        self.dto_pkg = base_package + ".dto"
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _emit_base_files(self):
        (self.out_dir / "BaseMapperConfig.java").write_text(
            BASE_MAPPER_CONFIG_TPL.format(pkg=self.pkg), encoding="utf-8"
        )
        (self.out_dir / "BaseAuditMapper.java").write_text(
            BASE_AUDIT_MAPPER_TPL.format(pkg=self.pkg, base=self.base), encoding="utf-8"
        )

    def _emit_mapper(self, t, target_pk_index):
        entity = to_camel(t.name)
        pk_cols = [c for c in t.columns if getattr(c, "primary_key", False)]
        has_composite_pk = len(pk_cols) > 1

        # Collect FK relations
        rels = []
        for c in t.columns:
            if getattr(c, "foreign_key_table", None) and getattr(c, "foreign_key_column", None):
                field_base = _relation_base(c.name)  # lowerCamel
                target_tbl = c.foreign_key_table
                target_entity = to_camel(target_tbl)
                pk_info = target_pk_index.get(target_tbl)  # (pk_field_lc, pk_java_type, setter) or None
                rels.append((c, field_base, target_tbl, target_entity, pk_info))

        imports: Set[str] = set()
        to_dto_mappings: List[str] = []
        to_entity_mappings: List[str] = []

        # ===== Composite PK handling =====
        if has_composite_pk:
            for c in pk_cols:
                col_lc = to_lower_camel(c.name)
                to_dto_mappings.append(f'@Mapping(source = "id.{col_lc}", target = "id.{col_lc}")')
                to_entity_mappings.append(f'@Mapping(source = "id.{col_lc}", target = "id.{col_lc}")')

            for c in pk_cols:
                if getattr(c, "foreign_key_table", None) and getattr(c, "foreign_key_column", None):
                    field_base = _relation_base(c.name)
                    # relation set is handled in service; ignore here
                    to_entity_mappings.append(f'@Mapping(target = "{field_base}", ignore = true)')

        # ===== Non-composite cases & non-PK relations =====
        for c, field_base, target_tbl, target_entity, pk_info in rels:
            if has_composite_pk and getattr(c, "primary_key", False):
                continue

            if pk_info:
                pk_field_lc, pk_java, setter = pk_info
                imports.add(f"import {self.entity_pkg}.{target_entity};")
                # entity -> dto: relation.<pk> -> <base>Id
                to_dto_mappings.append(
                    f'@Mapping(source = "{field_base}.{pk_field_lc}", target = "{field_base}Id")'
                )
                # dto -> entity: handled in service; ignore here
                to_entity_mappings.append(
                    f'@Mapping(target = "{field_base}", ignore = true)'
                )
            else:
                # Target with composite PK: ignore both sides for simplified id fields
                to_dto_mappings.append(f'@Mapping(target = "{field_base}Id", ignore = true)')
                to_entity_mappings.append(f'@Mapping(target = "{field_base}", ignore = true)')

        imports_block = "\n".join(sorted(imports))

        to_entity_ann = ""
        if to_entity_mappings:
            to_entity_ann = "    @Mappings({\n        " + ",\n        ".join(to_entity_mappings) + "\n    })\n"
        to_dto_ann = ""
        if to_dto_mappings:
            to_dto_ann = "    @Mappings({\n        " + ",\n        ".join(to_dto_mappings) + "\n    })\n"

        content = MAPPER_TPL.format(
            pkg=self.pkg,
            base=self.base,
            imports=(imports_block + "\n" if imports_block else ""),
            Entity=entity,
            to_entity_ann=to_entity_ann,
            to_dto_ann=to_dto_ann
        )
        (self.out_dir / f"{entity}Mapper.java").write_text(content, encoding="utf-8")

    def generate(self, tables: List):
        self._emit_base_files()
        target_pk_index = _build_target_pk_index(tables)
        for t in tables:
            self._emit_mapper(t, target_pk_index)
