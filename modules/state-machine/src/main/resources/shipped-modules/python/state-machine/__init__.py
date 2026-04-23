from fastapi import APIRouter, FastAPI


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    router = APIRouter(prefix="/api/modules/state-machine", tags=["state-machine"])

    @router.get("/health")
    def workflow_health() -> dict:
        return {
            "status": "UP",
            "module": "state-machine",
            "config": config or {},
        }

    app.include_router(router)
