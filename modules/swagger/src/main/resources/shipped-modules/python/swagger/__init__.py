from __future__ import annotations

from typing import Any

from fastapi import APIRouter, FastAPI


def resolve_app_config(config: dict[str, Any], manifest: dict[str, Any], default_title: str) -> dict[str, Any]:
    resolved = _resolve_config(config or {}, manifest or {}, default_title)
    return {
        "docs_url": resolved["docs_path"] if resolved["enable_ui"] else None,
        "openapi_url": resolved["openapi_path"],
        "title": resolved["title"],
        "description": resolved["description"],
        "version": resolved["version"],
    }


def register_module(app: FastAPI, config: dict[str, Any], manifest: dict[str, Any]) -> None:
    resolved = _resolve_config(config or {}, manifest or {}, app.title)
    router = APIRouter(prefix="/api/modules/swagger", tags=["swagger"])

    @router.get("/config")
    def swagger_config() -> dict[str, Any]:
        return {
            "title": resolved["title"],
            "description": resolved["description"],
            "version": resolved["version"],
            "docsPath": resolved["docs_path"],
            "openApiPath": resolved["openapi_path"],
            "enableUi": resolved["enable_ui"],
        }

    app.include_router(router)


def _resolve_config(config: dict[str, Any], manifest: dict[str, Any], default_title: str) -> dict[str, Any]:
    docs_path = str(config.get("docsPath") or "/swagger").strip() or "/swagger"
    openapi_path = str(config.get("openApiPath") or "/openapi.json").strip() or "/openapi.json"
    if not docs_path.startswith("/"):
        docs_path = "/" + docs_path
    if not openapi_path.startswith("/"):
        openapi_path = "/" + openapi_path

    selected_modules = manifest.get("selectedModules") or []
    return {
        "title": str(config.get("title") or default_title).strip() or default_title,
        "description": str(config.get("description") or f"Generated API modules: {', '.join(selected_modules)}").strip(),
        "version": str(config.get("version") or "1.0.0").strip() or "1.0.0",
        "docs_path": docs_path,
        "openapi_path": openapi_path,
        "enable_ui": bool(config.get("enableUi", True)),
    }
