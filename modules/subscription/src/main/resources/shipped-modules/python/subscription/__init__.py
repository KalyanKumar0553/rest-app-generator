from __future__ import annotations

from typing import Any

from fastapi import APIRouter, FastAPI, HTTPException
from pydantic import BaseModel


class SubscribeRequest(BaseModel):
    tenantId: str
    planCode: str


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    resolved = _resolve_config(config or {})
    router = APIRouter(prefix="/api/modules/subscription", tags=["subscription"])
    subscriptions: dict[str, dict[str, Any]] = {}

    @router.get("/health")
    def subscription_health() -> dict[str, Any]:
        return {
            "status": "UP",
            "module": "subscription",
            "currency": resolved["currency"],
            "defaultPlanCode": resolved["default_plan_code"],
            "plansCount": len(resolved["plans"]),
        }

    @router.get("/plans")
    def plans() -> dict[str, Any]:
        return {
            "currency": resolved["currency"],
            "plans": resolved["plans"],
        }

    @router.get("/current")
    def current_subscription(tenantId: str = "default-tenant") -> dict[str, Any]:
        tenant_key = tenantId.strip() or "default-tenant"
        return subscriptions.get(
            tenant_key,
            {
                "tenantId": tenant_key,
                "planCode": resolved["default_plan_code"],
                "status": "TRIAL" if resolved["allow_trial"] else "ACTIVE",
                "currency": resolved["currency"],
            },
        )

    @router.post("/subscribe")
    def subscribe(request: SubscribeRequest) -> dict[str, Any]:
        tenant_id = request.tenantId.strip()
        plan_code = request.planCode.strip().upper()
        plan = next((item for item in resolved["plans"] if item["code"] == plan_code), None)
        if plan is None:
            raise HTTPException(status_code=404, detail=f"Plan '{plan_code}' not found.")
        subscription = {
            "tenantId": tenant_id,
            "planCode": plan_code,
            "status": "ACTIVE",
            "currency": resolved["currency"],
        }
        subscriptions[tenant_id] = subscription
        return subscription

    app.include_router(router)


def _resolve_config(config: dict[str, Any]) -> dict[str, Any]:
    plans = config.get("plans") if isinstance(config.get("plans"), list) else None
    default_plan_code = str(config.get("defaultPlanCode") or "FREE").strip().upper() or "FREE"
    return {
        "currency": str(config.get("currency") or "INR").strip() or "INR",
        "default_plan_code": default_plan_code,
        "allow_trial": bool(config.get("allowTrial", True)),
        "plans": plans
        or [
            {"code": "FREE", "name": "Free", "monthlyPrice": 0},
            {"code": "PRO", "name": "Pro", "monthlyPrice": 499},
        ],
    }
