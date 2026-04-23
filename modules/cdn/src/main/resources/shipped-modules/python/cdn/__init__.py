from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any
from uuid import uuid4

from fastapi import APIRouter, FastAPI, File, HTTPException, UploadFile
from fastapi.staticfiles import StaticFiles

try:
    from azure.storage.blob import BlobServiceClient
except Exception:  # pragma: no cover
    BlobServiceClient = None


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    resolved = _resolve_config(config or {})
    router = APIRouter(prefix="/api/modules/cdn", tags=["cdn"])
    manifest_path = Path(resolved["local_directory"]) / ".manifest" / "assets.json"

    if resolved["provider"] == "local" and resolved["public_base_url"].startswith("/"):
        _mount_static_assets(app, resolved)

    @router.get("/health")
    def cdn_health() -> dict[str, Any]:
        return {
            "status": "UP",
            "provider": resolved["provider"],
            "maxFileSizeBytes": resolved["max_file_size_bytes"],
            "allowedMimeTypes": resolved["allowed_mime_types"],
        }

    @router.get("/config")
    def cdn_config() -> dict[str, Any]:
        return {
            "provider": resolved["provider"],
            "localDirectory": resolved["local_directory"],
            "publicBaseUrl": resolved["public_base_url"],
            "maxFileSizeBytes": resolved["max_file_size_bytes"],
            "allowedMimeTypes": resolved["allowed_mime_types"],
            "azureContainerName": resolved["azure_container_name"],
            "azureBlobPrefix": resolved["azure_blob_prefix"],
        }

    @router.get("/assets")
    def list_assets() -> list[dict[str, Any]]:
        return _read_manifest(manifest_path)

    @router.post("/uploads", status_code=201)
    async def upload_asset(file: UploadFile = File(...)) -> dict[str, Any]:
        if file.content_type not in resolved["allowed_mime_types"]:
            raise HTTPException(status_code=400, detail=f"Unsupported content type '{file.content_type}'.")

        payload = await file.read()
        if len(payload) > resolved["max_file_size_bytes"]:
            raise HTTPException(status_code=413, detail="File exceeds configured CDN upload limit.")

        record = _upload_asset(resolved, file.filename or "asset.bin", file.content_type or "application/octet-stream", payload)
        assets = _read_manifest(manifest_path)
        assets.insert(0, record)
        manifest_path.parent.mkdir(parents=True, exist_ok=True)
        manifest_path.write_text(json.dumps(assets, indent=2), encoding="utf-8")
        return record

    app.include_router(router)


def _resolve_config(config: dict[str, Any]) -> dict[str, Any]:
    provider = str(config.get("provider") or "local").strip().lower()
    local_directory = str(config.get("localDirectory") or "storage/cdn").strip() or "storage/cdn"
    public_base_url = str(config.get("publicBaseUrl") or "/cdn-assets").strip() or "/cdn-assets"
    if not public_base_url.startswith("/"):
        public_base_url = "/" + public_base_url
    allowed_mime_types = config.get("allowedMimeTypes") or [
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
    ]
    max_file_size_bytes = int(config.get("maxFileSizeBytes") or 10 * 1024 * 1024)
    azure_connection_string = str(config.get("azureConnectionString") or "").strip()
    azure_container_name = str(config.get("azureContainerName") or "cdn-assets").strip() or "cdn-assets"
    azure_blob_prefix = str(config.get("azureBlobPrefix") or "uploads").strip() or "uploads"

    if provider == "azure" and not azure_connection_string:
        raise RuntimeError("CDN provider is set to azure but azureConnectionString is missing.")

    return {
        "provider": "azure" if provider == "azure" else "local",
        "local_directory": local_directory,
        "public_base_url": public_base_url,
        "allowed_mime_types": allowed_mime_types,
        "max_file_size_bytes": max_file_size_bytes,
        "azure_connection_string": azure_connection_string,
        "azure_container_name": azure_container_name,
        "azure_blob_prefix": azure_blob_prefix,
    }


def _mount_static_assets(app: FastAPI, resolved: dict[str, Any]) -> None:
    mount_path = resolved["public_base_url"]
    for route in app.routes:
        if getattr(route, "path", None) == mount_path:
            return
    Path(resolved["local_directory"]).mkdir(parents=True, exist_ok=True)
    app.mount(mount_path, StaticFiles(directory=resolved["local_directory"]), name="generated-cdn-assets")


def _read_manifest(manifest_path: Path) -> list[dict[str, Any]]:
    if not manifest_path.exists():
        return []
    try:
        raw = json.loads(manifest_path.read_text(encoding="utf-8"))
        return raw if isinstance(raw, list) else []
    except json.JSONDecodeError:
        return []


def _upload_asset(resolved: dict[str, Any], original_name: str, content_type: str, payload: bytes) -> dict[str, Any]:
    storage_key = _build_storage_key(original_name, resolved["azure_blob_prefix"])
    if resolved["provider"] == "azure":
        if BlobServiceClient is None:
            raise RuntimeError("azure-storage-blob is not installed.")
        blob_service_client = BlobServiceClient.from_connection_string(resolved["azure_connection_string"])
        container_client = blob_service_client.get_container_client(resolved["azure_container_name"])
        try:
            container_client.create_container()
        except Exception:
            pass
        blob_client = container_client.get_blob_client(storage_key)
        blob_client.upload_blob(payload, overwrite=True, content_type=content_type)
        url = blob_client.url
        provider = "azure"
    else:
        target = Path(resolved["local_directory"]) / storage_key
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        url = f"{resolved['public_base_url'].rstrip('/')}/{storage_key}"
        provider = "local"

    return {
        "id": str(uuid4()),
        "originalName": original_name,
        "storageKey": storage_key,
        "url": url,
        "contentType": content_type,
        "size": len(payload),
        "provider": provider,
        "uploadedAt": datetime.now(timezone.utc).isoformat(),
    }


def _build_storage_key(original_name: str, blob_prefix: str) -> str:
    suffix = Path(original_name).suffix.lower()
    stem = Path(original_name).stem.lower().replace(" ", "-")
    safe_stem = "".join(character if character.isalnum() or character == "-" else "-" for character in stem).strip("-") or "asset"
    date_prefix = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    parts = [blob_prefix.strip("/"), date_prefix, f"{safe_stem}-{uuid4()}{suffix}"]
    return "/".join(part for part in parts if part)
