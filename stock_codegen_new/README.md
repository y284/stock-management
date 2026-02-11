# Stock Codegen (Path-aware)
Generate Liquibase + Spring layers from a small schema model.

## Configure Paths & Package
Export env vars or rely on defaults:
- `JAVA_OUT_DIR` (default: `/mnt/data/stock_codegen/out/java`)
- `JAVA_PACKAGE` (default: `com.example.stock`)
- `LB_OUT_FILE` (default: `/mnt/data/stock_codegen/db/changelog/master.xml`)
- `API_BASE` (default: `/api`)

## Run
```bash
python main.py
```

Outputs:
- Liquibase XML → `${LB_OUT_FILE}`
- Java code → `${JAVA_OUT_DIR}/${JAVA_PACKAGE}/(repository|service|service/impl|controller)`
