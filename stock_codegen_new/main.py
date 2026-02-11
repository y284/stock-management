import os
from pathlib import Path
from schema import tables
from liquibase_generator import write_versioned_files, write_master
from codegen.exceptions_spec import derive_exception_specs
from codegen.entities import EntityGenerator
from codegen.repositories import RepositoryGenerator
from codegen.services import ServiceGenerator
from codegen.controllers import ControllerGenerator
from codegen.dtos import DtoGenerator
from codegen.mappers import MapperGenerator
from codegen.errors import ErrorGenerator
from codegen.postman_gen import PostmanGenerator

# Config (project-relative defaults)
CWD = Path.cwd()
JAVA_OUT_DIR = Path(os.environ.get("JAVA_OUT_DIR", str(CWD / "out" / "java")))
PACKAGE = os.environ.get("JAVA_PACKAGE", "com.stock.stock_management")
VERSION_DIR = Path(os.environ.get("LB_DIR", str(CWD / "db" / "changelog" / "v_1_0_0")))
MASTER_FILE = Path(os.environ.get("LB_MASTER", str(CWD / "db" / "changelog" / "master.xml")))

def generate_all(ts):
    # Liquibase: per-table files and master includes (tables first, then constraints)
    table_files, constraint_files, ext_file = write_versioned_files(VERSION_DIR, ts, author="mehdi")
    include_files = [ext_file] + table_files + constraint_files
    write_master(MASTER_FILE, include_files)
    print(f"Wrote master: {MASTER_FILE}\nIncludes: {[f.name for f in include_files]}")
    # Compute exception spec once
    exception_spec = derive_exception_specs(ts)
    # Code generation
    ErrorGenerator(str(JAVA_OUT_DIR), PACKAGE).generate(ts)
    EntityGenerator(str(JAVA_OUT_DIR), PACKAGE).generate(ts)
    RepositoryGenerator(str(JAVA_OUT_DIR), PACKAGE, exception_spec=exception_spec, all_tables=ts).generate(ts)
    ServiceGenerator(str(JAVA_OUT_DIR), PACKAGE, exception_spec=exception_spec, all_tables=ts).generate(ts)
    DtoGenerator(str(JAVA_OUT_DIR), PACKAGE).generate(ts)
    MapperGenerator(str(JAVA_OUT_DIR), PACKAGE).generate(ts)
    ControllerGenerator(str(JAVA_OUT_DIR), PACKAGE, base_path=os.environ.get("API_BASE","/api")).generate(ts)
    print(f"Java code written under: {JAVA_OUT_DIR}/{PACKAGE.replace('.', '/')}/")
    
    PostmanGenerator(
        base_url=os.environ.get("POSTMAN_BASE_URL", "http://localhost:8080"),
        base_path=os.environ.get("API_BASE", "/api"),
        collection_out=os.environ.get("POSTMAN_COLLECTION_OUT", "stock_management.postman_collection.json"),
        env_out=os.environ.get("POSTMAN_ENV_OUT", "stock_management.postman_environment.json"),
        collection_name=os.environ.get("POSTMAN_COLLECTION_NAME", "Stock Management API"),
        environment_name=os.environ.get("POSTMAN_ENV_NAME", "Local Dev")
    ).generate(ts)

if __name__ == "__main__":
    generate_all(tables)
