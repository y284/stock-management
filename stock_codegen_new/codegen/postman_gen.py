# codegen/postman_gen.py
import json
from pathlib import Path
from typing import List, Dict, Any
from .utils import to_camel

POSTMAN_VERSION = "2.1.0"
BASE_COLUMNS = {"id", "uuid", "created_at", "updated_at", "version"}

def _sample_value(sql_type: str):
    s = (sql_type or "").upper().strip()
    if s.startswith(("VARCHAR", "CHAR", "TEXT")):
        return "string"
    if s.startswith("UUID"):
        return "{{uuid}}"
    if s.startswith(("NUMERIC", "DECIMAL")):
        return 0.0
    if s.startswith(("BIGINT", "INT", "SMALLINT")):
        return 1
    if s.startswith("BOOLEAN"):
        return False
    if "TIMESTAMP" in s or "DATE" in s or "TIME" in s:
        return "2025-01-01T00:00:00Z"
    return "value"

def _body_from_table(t) -> Dict[str, Any]:
    body: Dict[str, Any] = {}
    for c in getattr(t, "columns", []):
        name = c.name
        if name in BASE_COLUMNS or getattr(c, "primary_key", False):
            continue
        if getattr(c, "foreign_key_table", None) and getattr(c, "foreign_key_column", None):
            base = name[:-3] if name.endswith("_id") else name
            body[f"{base}Id"] = 1
        else:
            body[name] = _sample_value(getattr(c, "type", "") or "")
    return body

def _curl_for(method: str, url: str, body: Dict[str, Any] | None):
    # Basic-auth curl using collection environment variables
    base = f'curl -u "{{{{username}}}}:{{{{password}}}}" -X {method} "{{{{baseUrl}}}}{url}"'
    if body is None:
        return base
    return base + " \\\n  -H \"Content-Type: application/json\" \\\n  -d '" + json.dumps(body, indent=2) + "'"

class PostmanGenerator:
    def __init__(
        self,
        base_url: str = "http://localhost:8080",
        base_path: str = "/api",
        collection_out: str = "stock_management.postman_collection.json",
        env_out: str = "stock_management.postman_environment.json",
        collection_name: str = "Stock Management API",
        environment_name: str = "Local Dev",
    ):
        self.base_url = base_url.rstrip("/")
        self.base_path = base_path.rstrip("/")
        self.collection_out = Path(collection_out)
        self.env_out = Path(env_out)
        self.collection_name = collection_name
        self.environment_name = environment_name

    # ---------- Environment ----------
    def _environment(self):
        return {
            "id": "auto-generated-env",
            "name": self.environment_name,
            "values": [
                {"key": "baseUrl", "value": self.base_url, "type": "default", "enabled": True},
                {"key": "username", "value": "admin", "type": "secret", "enabled": True},
                {"key": "password", "value": "admin", "type": "secret", "enabled": True},
                {"key": "uuid", "value": "00000000-0000-0000-0000-000000000000", "type": "default", "enabled": True},
            ],
            "_postman_variable_scope": "environment",
            "_postman_exported_using": "codegen",
        }

    # ---------- Collection ----------
    def _collection_skeleton(self):
        return {
            "info": {
                "name": self.collection_name,
                "_postman_id": "auto-generated",
                "description": "Auto-generated collection for Spring Boot API testing (Basic Auth). "
                               "Set username/password in the environment.",
                "schema": f"https://schema.getpostman.com/json/collection/v{POSTMAN_VERSION}/collection.json",
            },
            "auth": {
                "type": "basic",
                "basic": [
                    {"key": "username", "value": "{{username}}", "type": "string"},
                    {"key": "password", "value": "{{password}}", "type": "string"},
                ],
            },
            "variable": [
                {"key": "baseUrl", "value": self.base_url},
                {"key": "uuid", "value": "00000000-0000-0000-0000-000000000000"},
            ],
            "item": [],
        }

    def _req(self, name: str, method: str, url_path: str, body: Dict[str, Any] | None = None, help_text: str = ""):
        raw = "{{baseUrl}}" + url_path
        req = {
            "name": name,
            "request": {
                "description": help_text,
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {"raw": raw, "host": ["{{baseUrl}}"], "path": url_path.strip("/").split("/")},
            },
            "response": [
                # Put a tiny example response shell so users see "Examples" tab
                {
                    "name": f"Example {name}",
                    "status": "OK",
                    "code": 200,
                    "header": [],
                    "body": "{}",
                }
            ],
        }
        if body is not None:
            req["request"]["body"] = {"mode": "raw", "raw": json.dumps(body, indent=2)}
        return req

    def _folder_for_table(self, t):
        entity = to_camel(t.name)
        path = t.name.replace("_", "-")
        url_base = f"{self.base_path}/{path}"

        pk_cols = [c.name for c in t.columns if getattr(c, "primary_key", False)]
        if not pk_cols:
            pk_cols = ["id"]
        id_path = "/".join([f"{{{c}}}" for c in pk_cols]) if len(pk_cols) > 1 else "{id}"
        id_desc = (
            "/".join([f"{{{c}}}" for c in pk_cols]) if len(pk_cols) > 1
            else "{id}"
        )

        sample = _body_from_table(t)

        items = []

        # List All
        items.append(self._req(
            "List All", "GET", f"{url_base}",
            help_text=_curl_for("GET", f"{url_base}", None)
        ))

        # List Page
        items.append(self._req(
            "List Page", "GET", f"{url_base}/page",
            help_text=_curl_for("GET", f"{url_base}/page?size=20&page=0&sort=id,desc", None)
        ))

        # List Sorted
        items.append(self._req(
            "List Sorted", "GET", f"{url_base}/sorted",
            help_text=_curl_for("GET", f"{url_base}/sorted?sort=id,desc", None)
        ))

        # Get by ID
        items.append(self._req(
            f"Get by ID ({id_desc})", "GET", f"{url_base}/{id_path}",
            help_text=_curl_for("GET", f"{url_base}/" + id_desc, None)
        ))

        # Get by UUID
        items.append(self._req(
            "Get by UUID", "GET", f"{url_base}/uuid/{{uuid}}",
            help_text=_curl_for("GET", f"{url_base}/uuid/{{uuid}}", None)
        ))

        # Create
        items.append(self._req(
            "Create", "POST", f"{url_base}", body=sample or {"note": "fill body"},
            help_text=_curl_for("POST", f"{url_base}", sample or {"note": "fill body"})
        ))

        # Update (PUT)
        items.append(self._req(
            f"Update (PUT) {id_desc}", "PUT", f"{url_base}/{id_path}", body=sample or {"note": "fill body"},
            help_text=_curl_for("PUT", f"{url_base}/" + id_desc, sample or {"note": "fill body"})
        ))

        # Patch
        items.append(self._req(
            f"Patch (Partial) {id_desc}", "PATCH", f"{url_base}/{id_path}", body=sample or {"note": "fill body"},
            help_text=_curl_for("PATCH", f"{url_base}/" + id_desc, sample or {"note": "fill body"})
        ))

        # Delete by ID
        items.append(self._req(
            f"Delete by ID {id_desc}", "DELETE", f"{url_base}/{id_path}",
            help_text=_curl_for("DELETE", f"{url_base}/" + id_desc, None)
        ))

        # Delete by UUID
        items.append(self._req(
            "Delete by UUID", "DELETE", f"{url_base}/uuid/{{uuid}}",
            help_text=_curl_for("DELETE", f"{url_base}/uuid/{{uuid}}", None)
        ))

        return {"name": entity, "item": items}

    # ---------- public ----------
    def generate(self, tables: List):
        # collection
        collection = self._collection_skeleton()
        for t in tables:
            collection["item"].append(self._folder_for_table(t))
        self.collection_out.write_text(json.dumps(collection, indent=2), encoding="utf-8")

        # environment
        env = self._environment()
        self.env_out.write_text(json.dumps(env, indent=2), encoding="utf-8")

        print(f"✅ Postman collection: {self.collection_out.absolute()}")
        print(f"✅ Postman environment: {self.env_out.absolute()}")
