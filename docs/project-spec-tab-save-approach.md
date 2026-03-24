# Project Spec Tab-Level Save Approach

## Context

Today the flow is centered on a full YAML payload:

- UX builds the entire YAML on demand in [`src/ux/src/app/modules/project-generation/components/project-generation-dashboard/project-generation-dashboard.component.ts`](/Users/ky/Workspace/rest-app-generator/src/ux/src/app/modules/project-generation/components/project-generation-dashboard/project-generation-dashboard.component.ts) and [`src/ux/src/app/modules/project-generation/components/node-project-generation-dashboard/node-project-generation-dashboard.component.ts`](/Users/ky/Workspace/rest-app-generator/src/ux/src/app/modules/project-generation/components/node-project-generation-dashboard/node-project-generation-dashboard.component.ts).
- Mapping is done in [`src/ux/src/app/modules/project-generation/services/project-spec-mapper.service.ts`](/Users/ky/Workspace/rest-app-generator/src/ux/src/app/modules/project-generation/services/project-spec-mapper.service.ts).
- Backend create/update endpoints accept raw YAML strings in [`api/src/main/java/com/src/main/controller/ProjectController.java`](/Users/ky/Workspace/rest-app-generator/api/src/main/java/com/src/main/controller/ProjectController.java).
- Persistence is also YAML-centric because [`api/src/main/java/com/src/main/model/ProjectEntity.java`](/Users/ky/Workspace/rest-app-generator/api/src/main/java/com/src/main/model/ProjectEntity.java) stores a single `yaml` column.

This means `Save Project` currently does three things at once:

1. Assemble the entire spec.
2. Validate it as one payload.
3. Persist it and optionally trigger generation.

That is acceptable now, but it becomes fragile as spec size and tab complexity grow.

## Question

Should we save and validate spec per tab, then use saved tab-level data during final zip generation instead of sending one huge YAML during `Save Project`?

## Recommendation

Yes, but not as "store YAML per tab".

The better direction is:

- Persist structured tab payloads per section.
- Validate per tab when the user leaves the tab or explicitly saves that section.
- Build final YAML only on the backend, from the persisted structured sections, when generation starts.

## Why not save YAML per tab directly

Saving tab-level YAML fragments sounds simple, but it introduces a few problems:

- YAML sections are not independent in this app. Some values are derived across tabs.
  Example: entity data, controller config, REST spec mapping, DTOs, mappers, profiles, actuator options.
- Cross-tab invariants would move into fragment merge logic, which is harder to reason about than the current single mapper.
- The current generator pipeline still expects one complete YAML map. Fragment YAML would still need merge, normalization, and conflict resolution somewhere.
- Partial YAML fragments are harder to version than structured JSON/tab DTOs.

So the root issue is not "full YAML is bad". The issue is "full YAML is currently the persistence contract". That is what should change.

## Better Target Design

Use a three-layer model:

### 1. Draft state layer

Persist a project draft as structured data, split by logical section:

- `general`
- `database`
- `entities`
- `relations`
- `dtos`
- `enums`
- `mappers`
- `controllers`
- `preferences`
- `actuator`

This can live as:

- one JSON column with sectioned shape, or
- multiple columns / child tables, or
- one `project_spec_draft` JSON document plus metadata columns.

For the current codebase, one backend draft document is the lowest-risk first step.

### 2. Validation layer

Validate in two passes:

- Tab-level validation on save of that tab.
- Full-project validation before generate.

Reason:

- A tab can be internally valid but still incompatible with another tab.
- Example: controller config references a model that no longer exists.
- Example: mapper points to a DTO removed in another tab.
- Example: database choice and entity options become incompatible for generator language/runtime.

So per-tab validation should improve UX and reduce late failures, but it must not replace final aggregate validation.

### 3. Generation layer

When user clicks `Save Project` or `Generate`:

- Backend loads saved draft sections.
- Backend assembles canonical project model.
- Backend runs full validation.
- Backend converts canonical model to YAML once.
- Existing generator pipeline continues to consume one final YAML/map.

This preserves the current state-machine/generator contract and avoids a large rewrite.

## Proposed API Direction

### Near-term

Add section-oriented draft endpoints, for example:

- `PUT /api/projects/{id}/draft/general`
- `PUT /api/projects/{id}/draft/database`
- `PUT /api/projects/{id}/draft/entities`
- `PUT /api/projects/{id}/draft/controllers`
- `GET /api/projects/{id}/draft`

Each endpoint:

- accepts structured JSON, not YAML
- validates only its owned section plus cheap referential checks if needed
- returns normalized saved payload and validation issues

### Generate path

Keep generation endpoints conceptually similar:

- `POST /api/projects`
- `POST /api/projects/{id}/generate`
- `POST /api/projects/{id}/save-and-generate`

But internally:

- creation/update stores draft sections
- generate builds YAML from persisted draft instead of trusting the browser to send one giant YAML blob

## Migration Strategy

Do this in phases.

### Phase 1

Add a backend draft model alongside existing YAML storage.

- Keep `ProjectEntity.yaml` for backward compatibility.
- Introduce a JSON draft payload or companion draft table.
- Continue generating YAML in the backend from draft, then also store final YAML for legacy consumers.

### Phase 2

Move UX to tab save.

- Each tab saves section data when user clicks save, next, or leaves the tab.
- UI shows section save state and section validation state.
- `Save Project` becomes mostly "finalize + generate", not "ship the whole design to backend".

### Phase 3

Reduce dependence on raw persisted YAML.

- Treat YAML as generated artifact/canonical snapshot, not source of truth.
- Keep it only if useful for preview, download, audit, or compatibility.

## Benefits

- Smaller request payloads per interaction.
- Earlier validation feedback.
- Lower chance of losing all progress on one large invalid submission.
- Backend becomes source of truth for canonical assembly.
- Generator pipeline can remain mostly unchanged.

## Risks and Design Constraints

- Cross-tab validation still exists, so this is not purely independent tab storage.
- Autosave can create many writes; debounce and dirty-checking are needed.
- Concurrent edits become more visible once draft sections are persisted incrementally.
- Draft schema versioning is required if UI structure evolves.
- Existing load flow currently expects `project.yaml`; loading from structured draft must be designed carefully.

## Important Architectural Note

Do not implement this as a frontend-only optimization where UX still owns canonical assembly and simply sends multiple partial payloads.

That would spread spec-building logic across tabs and create another class of consistency bugs.

The canonical assembly should move toward backend orchestration, while the frontend becomes responsible for:

- collecting section input
- saving section drafts
- showing section validation
- requesting final generation

## Practical Conclusion

The idea is valid, but the right implementation is:

- save structured spec by tab
- validate by tab
- re-validate globally before generate
- assemble final YAML on backend from persisted draft

Not:

- save independent YAML fragments per tab and stitch them together ad hoc

## Suggested First Implementation Slice

If this is taken forward, the first slice should be:

1. Introduce backend draft storage for `general`, `database`, and `preferences`.
2. Add one section save endpoint and validation response format.
3. Load UI from draft when present, else fall back to existing YAML.
4. Keep final generation pipeline unchanged except for deriving YAML from draft on backend.

This keeps the refactor incremental and avoids breaking the existing generation engine early.
