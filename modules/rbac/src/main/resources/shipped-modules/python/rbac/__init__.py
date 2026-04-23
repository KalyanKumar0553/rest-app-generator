from fastapi import APIRouter, FastAPI


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    router = APIRouter(prefix="/api/modules/rbac", tags=["rbac"])

    @router.get("/health")
    def rbac_health() -> dict:
        return {
            "status": "UP",
            "module": "rbac",
            "config": config or {},
        }

    app.include_router(router)
