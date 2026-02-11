from schema import tables

from codegen.exceptions_spec import derive_exception_specs

print(derive_exception_specs(tables))