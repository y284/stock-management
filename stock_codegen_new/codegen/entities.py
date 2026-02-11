from pathlib import Path
from typing import List
from .utils import (
    to_camel, to_lower_camel, java_type, column_annotation,
    fk_field_name, pk_generation_strategy, table_indexes, escape_quotes
)

BASE_COLUMNS = {"uuid", "created_at", "updated_at", "version"}

BASE_ENTITY_TPL = """package {pkg}.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity implements java.io.Serializable {{

    @NaturalId
    @Column(name = "uuid", nullable = false, updatable = false, unique = true, columnDefinition = "UUID")
    private UUID uuid;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    protected void onPrePersist() {{
        if (this.uuid == null) this.uuid = UUID.randomUUID();
    }}

    public void softDelete() {{
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
    }}

    @Override
    public boolean equals(Object o) {{
        if (this == o) return true;
        if (!(o instanceof BaseEntity that)) return false;
        return uuid != null && uuid.equals(that.getUuid());
    }}

    @Override
    public int hashCode() {{
        return Objects.hash(uuid);
    }}
}}
"""
EMBEDDABLE_ID_TPL = """package {pkg}.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class {id_name}{{

{id_fields}
}}
"""

ENTITY_TPL = """package {pkg}.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@SQLDelete(sql = "UPDATE {table_name} SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "{table_name}"{schema_part},
    uniqueConstraints = {{
        @UniqueConstraint(name = "uk_{table_name}_uuid", columnNames = {{"uuid"}})
    }},
    indexes = {{
{indexes}
    }}
)
public class {entity_name} extends BaseEntity {{

{fields}
}}
"""

FIELD_TPL = "    private {type} {name};\n\n"


class EntityGenerator:
    def __init__(self, out_dir: str, base_package: str):
        self.out_dir = Path(out_dir) / Path(*base_package.split(".")) / "entity"
        self.pkg = base_package
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _emit_base_entity(self):
        path = self.out_dir / "BaseEntity.java"
        if not path.exists():
            path.write_text(BASE_ENTITY_TPL.format(pkg=self.pkg), encoding="utf-8")

    def _emit_embeddable_id(self, t, pk_cols):
        id_name = f"{to_camel(t.name)}Id"
        id_fields = "".join(
            (column_annotation(c)
             + (f"    @Comment(\"{escape_quotes(c.remarks)}\")\n" if getattr(c, 'remarks', None) else "")
             + FIELD_TPL.format(type=java_type(c.type), name=to_lower_camel(c.name)))
            for c in pk_cols
        )
        content = EMBEDDABLE_ID_TPL.format(pkg=self.pkg, id_name=id_name, id_fields=id_fields)
        (self.out_dir / f"{id_name}.java").write_text(content, encoding="utf-8")
        return id_name

    def _emit_entity(self, t):
        entity_name = to_camel(t.name)
        pk_cols = [c for c in t.columns if getattr(c, "primary_key", False)]
        has_composite_pk = len(pk_cols) > 1
        fields = []

        if has_composite_pk:
            id_name = self._emit_embeddable_id(t, pk_cols)
            fields.append(f"    @EmbeddedId\n    private {id_name} id;\n\n")

        for c in t.columns:
            if c.name in BASE_COLUMNS:
                continue

            is_pk = getattr(c, "primary_key", False)
            fk_table = getattr(c, "foreign_key_table", None)

            # === Foreign Key ===
            if fk_table:
                rel_entity = to_camel(fk_table)
                rel_field = fk_field_name(c.name)
                maps_id = f'    @MapsId("{to_lower_camel(c.name)}")\n' if is_pk else ""
                ann = (
                    f"{maps_id}"
                    f"    @ManyToOne(fetch = FetchType.LAZY)\n"
                    f"    @JoinColumn(name = \"{c.name}\", nullable = "
                    f"{str(c.nullable).lower() if c.nullable is not None else 'true'})\n"
                )
                if getattr(c, "remarks", None):
                    ann += f'    @Comment("{escape_quotes(c.remarks)}")\n'
                fields.append(ann + FIELD_TPL.format(type=rel_entity, name=rel_field))
                continue

            # === Normal Field or Single PK ===
            jtype = java_type(c.type)
            ann = ""
            if is_pk and not has_composite_pk:
                ann += "    @Id\n" + pk_generation_strategy(jtype)
            ann += column_annotation(c)
            if getattr(c, "remarks", None):
                ann += f'    @Comment("{escape_quotes(c.remarks)}")\n'

            fields.append(ann + FIELD_TPL.format(type=jtype, name=to_lower_camel(c.name)))

        schema_part = f', schema = "{t.schema_name}"' if getattr(t, "schema_name", None) else ""
        indexes_str = ",\n".join(table_indexes(t))
        content = ENTITY_TPL.format(
            pkg=self.pkg,
            table_name=t.name,
            schema_part=schema_part,
            indexes=indexes_str,
            entity_name=entity_name,
            fields="".join(fields),
        )
        (self.out_dir / f"{entity_name}.java").write_text(content, encoding="utf-8")

    def generate(self, tables: List):
        self._emit_base_entity()
        for t in tables:
            self._emit_entity(t)
