from dataclasses import dataclass, field
from typing import List, Optional

@dataclass
class Column:
    name: str
    type: str
    default_value: Optional[str] = None
    primary_key: bool = False
    foreign_key_table: Optional[str] = None
    foreign_key_column: Optional[str] = None
    is_index: bool = False
    nullable: bool | None = None
    unique: bool | None = None 

@dataclass
class Table:
    name: str
    schema_name: Optional[str] = "public"
    remarks: Optional[str] = None
    columns: List[Column] = field(default_factory=list)
