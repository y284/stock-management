from pathlib import Path
from typing import List
from .utils import to_camel, to_lower_camel, java_type

CTRL_TPL_SINGLE = """package {pkg};

import {base}.dto.{Entity}Dto;
import {base}.service.{Entity}Service;
import {base}.error.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.*;
import java.util.UUID;

@RestController
@Validated
@RequestMapping(path = "{base_path}/{path}", produces = MediaType.APPLICATION_JSON_VALUE)
public class {Entity}Controller {{

    private final {Entity}Service service;

    public {Entity}Controller({Entity}Service service) {{
        this.service = service;
    }}

    // ===== Read =====
    @GetMapping
    public List<{Entity}Dto> list() {{
        return service.findAll();
    }}

    @GetMapping("/page")
    public Page<{Entity}Dto> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Sort sort) {{
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.findAll(pageable);
    }}

    @GetMapping("/sorted")
    public List<{Entity}Dto> listSorted(Sort sort) {{
        return service.findAll(sort);
    }}

    @GetMapping("/{{id}}")
    public {Entity}Dto get(@PathVariable {id_type} id) {{
        return service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{EntityLower} not found with id=" + id));
    }}

    @GetMapping("/uuid/{{uuid}}")
    public {Entity}Dto getByUuid(@PathVariable UUID uuid) {{
        return service.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("{EntityLower} not found with uuid=" + uuid));
    }}

    // ===== Create =====
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<{Entity}Dto> create(@Valid @RequestBody {Entity}Dto dto) {{
        {Entity}Dto created = service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/uuid/{{id}}")
                .buildAndExpand(created.getUuid())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }}

    // ===== Update (full replace) =====
    @PutMapping(path = "/{{id}}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public {Entity}Dto update(@PathVariable {id_type} id, @Valid @RequestBody {Entity}Dto dto) {{
        return service.update(id, dto);
    }}

    // ===== Patch (partial) =====
    @PatchMapping(path = "/{{id}}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public {Entity}Dto patch(@PathVariable {id_type} id, @RequestBody {Entity}Dto dto) {{
        return service.patch(id, dto);
    }}

    // ===== Delete =====
    @DeleteMapping("/{{id}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable {id_type} id) {{
        service.deleteById(id);
    }}

    @DeleteMapping("/uuid/{{uuid}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUuid(@PathVariable UUID uuid) {{
        service.deleteByUuid(uuid);
    }}

    private org.springframework.data.domain.Sort parseSort(List<String> sorts) {{
        if (sorts == null || sorts.isEmpty()) return org.springframework.data.domain.Sort.unsorted();
        java.util.List<org.springframework.data.domain.Sort.Order> orders = new java.util.ArrayList<>();
        for (String s : sorts) {{
            if (s == null || s.isBlank()) continue;
            String[] parts = s.split(",", 2);
            String prop = parts[0].trim();
            String dir = parts.length > 1 ? parts[1].trim() : "asc";
            orders.add(new org.springframework.data.domain.Sort.Order(
                    "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC
                                                : org.springframework.data.domain.Sort.Direction.ASC,
                    prop));
        }}
        return org.springframework.data.domain.Sort.by(orders);
    }}
}}
"""

# Composite-key aware template with snake_case path vars bound to lowerCamel params
CTRL_TPL_COMPOSITE = """package {pkg};

import {base}.dto.{Entity}Dto;
import {base}.entity.{Entity}Id;
import {base}.service.{Entity}Service;
import {base}.error.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.*;
import java.util.UUID;

@RestController
@Validated
@RequestMapping(path = "{base_path}/{path}", produces = MediaType.APPLICATION_JSON_VALUE)
public class {Entity}Controller {{

    private final {Entity}Service service;

    public {Entity}Controller({Entity}Service service) {{
        this.service = service;
    }}

    // ===== Read =====
    @GetMapping
    public List<{Entity}Dto> list() {{
        return service.findAll();
    }}

    @GetMapping("/page")
    public Page<{Entity}Dto> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Sort sort) {{
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.findAll(pageable);
    }}

    @GetMapping("/sorted")
    public List<{Entity}Dto> listSorted(Sort sort) {{
        return service.findAll(sort);
    }}

    // --- ID parts in path ---
    @GetMapping("{id_path}")
    public {Entity}Dto get({id_params}) {{
        {Entity}Id id = new {Entity}Id({id_args});
        return service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("{EntityLower} not found with id parts"));
    }}

    @GetMapping("/uuid/{{uuid}}")
    public {Entity}Dto getByUuid(@PathVariable UUID uuid) {{
        return service.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("{EntityLower} not found with uuid=" + uuid));
    }}

    // ===== Create =====
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<{Entity}Dto> create(@Valid @RequestBody {Entity}Dto dto) {{
        {Entity}Dto created = service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/uuid/{{id}}")
                .buildAndExpand(created.getUuid())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }}

    // ===== Update (full replace) =====
    @PutMapping(path = "{id_path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public {Entity}Dto update({id_params}, @Valid @RequestBody {Entity}Dto dto) {{
        {Entity}Id id = new {Entity}Id({id_args});
        return service.update(id, dto);
    }}

    // ===== Patch (partial) =====
    @PatchMapping(path = "{id_path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public {Entity}Dto patch({id_params}, @RequestBody {Entity}Dto dto) {{
        {Entity}Id id = new {Entity}Id({id_args});
        return service.patch(id, dto);
    }}

    // ===== Delete =====
    @DeleteMapping("{id_path}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete({id_params}) {{
        {Entity}Id id = new {Entity}Id({id_args});
        service.deleteById(id);
    }}

    @DeleteMapping("/uuid/{{uuid}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUuid(@PathVariable UUID uuid) {{
        service.deleteByUuid(uuid);
    }}
}}
"""

class ControllerGenerator:
    def __init__(self, out_dir: str, base_package: str, base_path="/api"):
        self.out_dir = Path(out_dir) / Path(*base_package.split(".")) / "controller"
        self.pkg = base_package + ".controller"
        self.base = base_package
        self.base_path = base_path.rstrip("/")
        self.out_dir.mkdir(parents=True, exist_ok=True)

    def _pk_columns(self, t):
        return [c for c in t.columns if getattr(c, "primary_key", False)]

    def _id_type_single(self, t):
        for c in t.columns:
            if getattr(c, "primary_key", False):
                return java_type(c.type)
        return "Long"

    def _emit_controller(self, t):
        entity = to_camel(t.name)
        entity_lower = entity[0].lower() + entity[1:]
        path = t.name.replace("_", "-")
        pk_cols = self._pk_columns(t)

        if len(pk_cols) <= 1:
            content = CTRL_TPL_SINGLE.format(
                pkg=self.pkg,
                base=self.base,
                Entity=entity,
                EntityLower=entity_lower,
                base_path=self.base_path,
                path=path,
                id_type=self._id_type_single(t)
            )
        else:
            # Composite: build path, param decls with snake->lowerCamel, and ctor args
            id_path = "/" + "/".join(f"{{{c.name}}}" for c in pk_cols)
            id_params = ", ".join(
                f'@PathVariable("{c.name}") {java_type(c.type)} {to_lower_camel(c.name)}'
                for c in pk_cols
            )
            id_args = ", ".join(to_lower_camel(c.name) for c in pk_cols)

            content = CTRL_TPL_COMPOSITE.format(
                pkg=self.pkg,
                base=self.base,
                Entity=entity,
                EntityLower=entity_lower,
                base_path=self.base_path,
                path=path,
                id_path=id_path,
                id_params=id_params,
                id_args=id_args
            )

        (self.out_dir / f"{entity}Controller.java").write_text(content, encoding="utf-8")

    def generate(self, tables: List):
        for t in tables:
            self._emit_controller(t)
