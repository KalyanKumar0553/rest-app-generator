from fastapi import APIRouter, FastAPI


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    router = APIRouter(prefix="/api/modules/subscription", tags=["subscription"])

    @router.get("/health")
    def subscription_health() -> dict:
        return {
            "status": "UP",
            "module": "subscription",
            "config": config or {},
        }

    app.include_router(router)
