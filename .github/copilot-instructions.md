## Quick orientation

This is a Spring Boot backend (Java 21) for inventory/stock management. Key facts an AI agent should know up-front:

- Project root: Java sources in `src/main/java/com/stock/stock_management` (note underscore in the package name — see `HELP.md`).
- Build: Maven with the included wrapper (`./mvnw`). Java 21 is required (see `pom.xml`).
- DB migrations: Liquibase changelogs live at `src/main/resources/db/changelog/master.xml` and are applied at application startup (see `application.yml`).
- Mapping/conversion: MapStruct + Lombok are used heavily. Mapper interfaces live in `src/main/java/com/stock/stock_management/mapper` and share `BaseMapperConfig` / `BaseAuditMapper`.

## How to build / run / test

- Build and run locally (applies Liquibase migrations on startup):
  - `./mvnw clean package` — produces an executable jar
  - `./mvnw spring-boot:run` — run the app (reads `application.yml`, will run Liquibase against configured DB)
- Tests: `./mvnw test` (project includes H2 runtime for tests)
- Notes: `application.yml` sets `spring.jpa.hibernate.ddl-auto: validate` — the DB schema must match the entities (use migrations or an H2 test profile).

## Architectural highlights / conventions

- Layering: controllers -> service -> repository (Spring Data JPA) -> entities. DTOs and mappers are used to decouple HTTP payloads from entities.
- DTOs are in `src/main/java/com/stock/stock_management/dto` and entities in `src/main/java/com/stock/stock_management/entity`.
- Controllers typically return DTOs or `Page<DTO>`; the app enables `EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` in `StockManagementApplication.java` — prefer mapping repository `Page<Entity>` to `Page<DTO>` via MapStruct.
- When adding a new domain type:
  1. Create the JPA entity in `entity/`.
 2. Add Liquibase changes under `src/main/resources/db/changelog/v_*/` and include them from `master.xml` (migrations are authoritative for schema).
 3. Add a `Repository` in `repository/`, a `Service` in `service/`, a `Controller` in `controller/`, a `DTO` in `dto/` and a `Mapper` in `mapper/` (extend `BaseAuditMapper` or use `BaseMapperConfig`).

## MapStruct / Lombok / annotation processing

- MapStruct mappers are configured via `maven-compiler-plugin` annotation processors in `pom.xml`. Ensure your IDE has annotation processing enabled; otherwise generated mappers will be missing.
- Mapper interfaces: search for `@Mapper(config = BaseMapperConfig.class)` in `src/main/java/com/stock/stock_management/mapper` for examples.

## Database and migrations

- Migrations: `src/main/resources/db/changelog/master.xml` includes `v_1_0_0/*` files. Do not modify entities without adding corresponding changelogs — `ddl-auto: validate` will fail startup otherwise.
- Local dev: either run a local Postgres instance matching `application.yml`, or create a dev profile that switches to H2 for quick iteration (tests already include H2).

## Common pitfalls / project-specific gotchas

- Package name uses an underscore: `com.stock.stock_management`. Do not change it to `com.stock.stock-management` or `com.stock.stockmanagement`.
- MapStruct + Lombok interplay: if mappers don't generate, check `maven-compiler-plugin` config in `pom.xml` and ensure IDE annotation processing is on.
- Schema validation is strict (`validate`) — migrations must be applied before or during app start.

## Helpful files to inspect when working on features/bugs

- `pom.xml` — build, Java version, annotation processors
- `HELP.md` — discovered notes about package naming and references
- `src/main/resources/application.yml` — DB and Liquibase configuration
- `src/main/resources/db/changelog/master.xml` — migration order and included files
- `src/main/java/com/stock/stock_management/mapper/*` — MapStruct mapper examples
- `src/main/java/com/stock/stock_management/controller/*` and `service/*` — typical request flow

If anything here is unclear or you want the instructions tuned for a particular kind of task (e.g., adding new entities, debugging startup migration failures, or writing controller tests), tell me which area to expand and I'll iterate.
