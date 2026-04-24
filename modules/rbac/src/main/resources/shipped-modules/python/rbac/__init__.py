from fastapi import APIRouter, FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel


def _normalize_config(config: dict) -> dict:
    roles = config.get("roles") if isinstance(config.get("roles"), list) else []
    permissions = config.get("permissions") if isinstance(config.get("permissions"), list) else []
    role_permissions = config.get("rolePermissions") if isinstance(config.get("rolePermissions"), list) else []
    normalized_roles = []
    for role in roles:
        if not isinstance(role, dict):
            continue
        code = str(role.get("code", "")).strip().upper()
        if not code:
            continue
        normalized_roles.append(
            {
                "code": code,
                "displayName": str(role.get("displayName", "")).strip() or code,
                "description": str(role.get("description", "")).strip(),
                "systemRole": bool(role.get("systemRole", False)),
                "active": bool(role.get("active", True)),
            }
        )
    if not normalized_roles:
        normalized_roles = [
            {
                "code": "ROLE_USER",
                "displayName": "User",
                "description": "Standard authenticated access.",
                "systemRole": True,
                "active": True,
            },
            {
                "code": "ROLE_ADMIN",
                "displayName": "Admin",
                "description": "Administrative access.",
                "systemRole": True,
                "active": True,
            },
        ]

    normalized_permissions = []
    for permission in permissions:
        if not isinstance(permission, dict):
            continue
        code = str(permission.get("code", "")).strip()
        if not code:
            continue
        normalized_permissions.append(
            {
                "code": code,
                "displayName": str(permission.get("displayName", "")).strip() or code,
                "description": str(permission.get("description", "")).strip(),
                "category": str(permission.get("category", "RBAC")).strip() or "RBAC",
                "active": bool(permission.get("active", True)),
            }
        )
    if not normalized_permissions:
        normalized_permissions = [
            {
                "code": "project.read",
                "displayName": "View Projects",
                "description": "Read project details.",
                "category": "PROJECT",
                "active": True,
            },
            {
                "code": "project.manage",
                "displayName": "Manage Projects",
                "description": "Create and update project definitions.",
                "category": "PROJECT",
                "active": True,
            },
        ]

    normalized_role_permissions = []
    for mapping in role_permissions:
        if not isinstance(mapping, dict):
            continue
        role_code = str(mapping.get("roleCode", "")).strip().upper()
        permission_codes = mapping.get("permissionCodes") if isinstance(mapping.get("permissionCodes"), list) else []
        permission_codes = [str(code).strip() for code in permission_codes if str(code).strip()]
        if role_code and permission_codes:
            normalized_role_permissions.append({"roleCode": role_code, "permissionCodes": permission_codes})
    if not normalized_role_permissions:
        normalized_role_permissions = [
            {"roleCode": "ROLE_USER", "permissionCodes": ["project.read"]},
            {"roleCode": "ROLE_ADMIN", "permissionCodes": ["project.read", "project.manage"]},
        ]

    return {
        "defaultRole": str(config.get("defaultRole", "ROLE_USER")).strip().upper() or "ROLE_USER",
        "roles": normalized_roles,
        "permissions": normalized_permissions,
        "rolePermissions": normalized_role_permissions,
        "routes": [
            {
                "pathPattern": str(route.get("pathPattern", "")).strip(),
                "httpMethod": str(route.get("httpMethod", "")).strip().upper() or None,
                "authorities": [
                    str(authority).strip()
                    for authority in route.get("authorities", [])
                    if str(authority).strip()
                ],
                "priority": int(route.get("priority", 100)),
                "active": bool(route.get("active", True)),
            }
            for route in (config.get("routes") if isinstance(config.get("routes"), list) else [])
            if isinstance(route, dict)
            and str(route.get("pathPattern", "")).strip()
            and isinstance(route.get("authorities"), list)
            and any(str(authority).strip() for authority in route.get("authorities", []))
        ],
    }


class AssignRoleRequest(BaseModel):
    userId: str
    roleCode: str


def register_module(app: FastAPI, config: dict, _manifest: dict) -> None:
    router = APIRouter(prefix="/api/modules/rbac", tags=["rbac"])
    normalized_config = _normalize_config(config or {})
    assignments: dict[str, str] = {}
    permission_map = {
        item["roleCode"]: item["permissionCodes"] for item in normalized_config["rolePermissions"]
    }
    route_policies = sorted(
        [route for route in normalized_config["routes"] if route.get("active", True)],
        key=lambda item: int(item.get("priority", 100)),
    )

    @app.middleware("http")
    async def rbac_route_authority_guard(request, call_next):
        path = request.url.path
        method = request.method.strip().upper()
        matched_policy = None
        for policy in route_policies:
            method_matches = policy["httpMethod"] is None or policy["httpMethod"] == method
            if method_matches and _path_matches(policy["pathPattern"], path):
                matched_policy = policy
                break
        if matched_policy is None:
            return await call_next(request)
        current_role = (request.headers.get("x-role") or normalized_config["defaultRole"]).strip().upper()
        direct_authorities = [
            item.strip()
            for item in (request.headers.get("x-authorities") or "").split(",")
            if item.strip()
        ]
        effective_authorities = set(direct_authorities)
        effective_authorities.update(permission_map.get(current_role, []))
        if not any(authority in effective_authorities for authority in matched_policy["authorities"]):
            return JSONResponse(
                status_code=403,
                content={
                    "message": "RBAC authority denied this request.",
                    "requiredAuthorities": matched_policy["authorities"],
                    "pathPattern": matched_policy["pathPattern"],
                    "httpMethod": matched_policy["httpMethod"],
                },
            )
        return await call_next(request)

    @router.get("/health")
    def rbac_health() -> dict:
        return {
            "status": "UP",
            "module": "rbac",
        }

    @router.get("/roles")
    def roles() -> dict:
        permission_map = {item["roleCode"]: item["permissionCodes"] for item in normalized_config["rolePermissions"]}
        return {
            "defaultRole": normalized_config["defaultRole"],
            "roles": [
                {
                    **role,
                    "permissions": permission_map.get(role["code"], []),
                }
                for role in normalized_config["roles"]
            ],
            "permissions": normalized_config["permissions"],
        }

    @router.post("/assign")
    def assign_role(request: AssignRoleRequest) -> dict:
        role_code = request.roleCode.strip().upper()
        if role_code not in {role["code"] for role in normalized_config["roles"]}:
            raise HTTPException(status_code=404, detail=f"Role {role_code} not found.")
        assignments[request.userId.strip()] = role_code
        return {"userId": request.userId.strip(), "role": role_code}

    @router.get("/current")
    def current_role(userId: str = "default-user") -> dict:
        return {
            "userId": userId,
            "role": assignments.get(userId, normalized_config["defaultRole"]),
        }

    app.include_router(router)


def _path_matches(path_pattern: str, request_path: str) -> bool:
    pattern_parts = [part for part in path_pattern.split("/") if part]
    request_parts = [part for part in request_path.split("/") if part]
    return _match_path_parts(pattern_parts, request_parts)


def _match_path_parts(pattern_parts: list[str], request_parts: list[str]) -> bool:
    if not pattern_parts:
        return not request_parts
    if pattern_parts[0] == "**":
        if _match_path_parts(pattern_parts[1:], request_parts):
            return True
        return bool(request_parts) and _match_path_parts(pattern_parts, request_parts[1:])
    if not request_parts:
        return False
    if pattern_parts[0] == "*" or pattern_parts[0] == request_parts[0]:
        return _match_path_parts(pattern_parts[1:], request_parts[1:])
    return False
