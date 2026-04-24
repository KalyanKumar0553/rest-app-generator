from __future__ import annotations

from typing import Any

from fastapi import APIRouter, FastAPI, HTTPException
from pydantic import BaseModel


class WorkflowInstanceRequest(BaseModel):
    entityId: str


class WorkflowTransitionRequest(BaseModel):
    event: str


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    resolved = _resolve_config(config or {})
    router = APIRouter(prefix="/api/modules/state-machine", tags=["state-machine"])
    instances: dict[str, dict[str, Any]] = {}

    @router.get("/health")
    def workflow_health() -> dict[str, Any]:
        return {
            "status": "UP",
            "module": "state-machine",
            "workflowName": resolved["workflow_name"],
            "initialState": resolved["initial_state"],
            "states": resolved["states"],
            "transitions": resolved["transitions"],
        }

    @router.get("/workflow")
    def workflow_definition() -> dict[str, Any]:
        return {
            "workflowName": resolved["workflow_name"],
            "initialState": resolved["initial_state"],
            "states": resolved["states"],
            "transitions": resolved["transitions"],
            "instances": list(instances.values()),
        }

    @router.post("/workflow/instances", status_code=201)
    def create_instance(request: WorkflowInstanceRequest) -> dict[str, Any]:
        entity_id = request.entityId.strip()
        if not entity_id:
            raise HTTPException(status_code=400, detail="entityId is required.")
        instance = {
            "entityId": entity_id,
            "currentState": resolved["initial_state"],
            "history": [resolved["initial_state"]],
        }
        instances[entity_id] = instance
        return instance

    @router.post("/workflow/instances/{entity_id}/transition")
    def transition_instance(entity_id: str, request: WorkflowTransitionRequest) -> dict[str, Any]:
        entity_key = entity_id.strip()
        event_name = request.event.strip()
        if entity_key not in instances:
            raise HTTPException(status_code=404, detail=f"Workflow instance '{entity_key}' not found.")
        transition = next(
            (
                item
                for item in resolved["transitions"]
                if item["from"] == instances[entity_key]["currentState"] and item["event"] == event_name
            ),
            None,
        )
        if transition is None:
            raise HTTPException(
                status_code=400,
                detail=f"Transition '{event_name}' is not allowed from '{instances[entity_key]['currentState']}'.",
            )
        instances[entity_key]["currentState"] = transition["to"]
        instances[entity_key]["history"].append(transition["to"])
        return instances[entity_key]

    app.include_router(router)


def _resolve_config(config: dict[str, Any]) -> dict[str, Any]:
    states = config.get("states") if isinstance(config.get("states"), list) else None
    transitions = config.get("transitions") if isinstance(config.get("transitions"), list) else None
    return {
        "workflow_name": str(config.get("workflowName") or "default-workflow").strip() or "default-workflow",
        "initial_state": str(config.get("initialState") or "draft").strip() or "draft",
        "states": states
        or [
            {"id": "draft", "label": "Draft"},
            {"id": "in_review", "label": "In Review"},
            {"id": "published", "label": "Published"},
        ],
        "transitions": transitions
        or [
            {"from": "draft", "event": "submit", "to": "in_review"},
            {"from": "in_review", "event": "approve", "to": "published"},
            {"from": "in_review", "event": "reject", "to": "draft"},
        ],
    }
