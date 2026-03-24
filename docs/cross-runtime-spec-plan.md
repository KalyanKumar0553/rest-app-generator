# Cross-Runtime Spec Plan

## Goal

Support generating the same project behavior across Java, Node, and later Python from one canonical spec.

## Spec Shape

The backend now emits a layered project spec:

- top-level fields: retained for backward compatibility with existing generators
- `core`: runtime-agnostic behavior and domain definition
- `runtime`: runtime-specific overlays

## Core Section

`core` is intended to stay language-neutral and reusable when migrating a project from one runtime to another.

Current structure:

- `core.app`
  - `name`
  - `description`
  - `artifactId`
- `core.database`
  - `database`
  - `dbType`
  - `dbGeneration`
  - `pluralizeTableNames`
- `core.domain`
  - `models`
  - `dtos`
  - `enums`
  - `mappers`
- `core.api`
  - `restSpec`
  - `controllersEnabled`
- `core.modules`
  - `selected`
  - `customDependencies`
  - `config`

## Runtime Section

`runtime` contains overlays for each supported runtime and an active runtime marker.

Current structure:

- `runtime.active`
- `runtime.java`
- `runtime.node`
- `runtime.python`

### Java

- `packageName`
- `groupId`
- `artifactId`
- `buildTool`
- `javaVersion`
- `enableOpenapi`
- `enableActuator`
- `enableLombok`
- `packageStructure`

### Node

- `packageName`
- `packageManager`
- `port`
- `framework`
- `orm`
- `docker`

### Python

Reserved for later onboarding:

- `packageName`
- `framework`
- `orm`
- `migrations`
- `entrypoint`

## Compatibility Strategy

The generators still read the existing top-level keys today. During the transition:

1. backend emits both flat and layered spec formats
2. generators begin reading `runtime.*` and `core.*` where safe
3. once all generators use layered sections reliably, flat duplication can be reduced

## Python Onboarding Plan

Python onboarding is deferred, but the intended stack is:

- FastAPI
- Pydantic
- SQLAlchemy
- Alembic

Implementation order when resumed:

1. UX language onboarding for `python`
2. shared editor capability flags for Python
3. canonical spec consumption via `core` + `runtime.python`
4. Python scaffold and config generation
5. model/schema/router/service generation
6. Alembic migration generation
7. generated project smoke tests

## Migration Goal

Long-term target:

- create in Java
- regenerate in Node
- later regenerate in Python

without redefining domain behavior, API intent, security intent, or module selection.
