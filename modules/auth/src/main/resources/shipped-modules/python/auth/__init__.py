from fastapi import APIRouter, FastAPI


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    router = APIRouter(prefix="/api/modules/auth", tags=["auth"])

    @router.get("/health")
    def auth_health() -> dict:
        return {
            "status": "UP",
            "module": "auth",
            "config": config or {},
        }

    app.include_router(router)
