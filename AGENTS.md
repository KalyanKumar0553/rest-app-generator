# AGENTS.md

## Project Working Rules

### UX Theming (Mandatory)
- Do not use hardcoded color values in UX code (no hex/rgb/rgba literals for component styling).
- Always use centralized theme tokens from:
  - `/Users/ky/Workspace/rest-app-generator/src/ux/src/app/styles/colors.css`
  - `/Users/ky/Workspace/rest-app-generator/src/ux/src/app/styles/dark-theme.css`
- Prefer existing token variables first (`--color-*`, `--theme-*`).
- If a new color is needed, add a new token in centralized theme files, then consume that token in components.
- For runtime/theme-preference-driven updates, use theme services:
  - `/Users/ky/Workspace/rest-app-generator/src/ux/src/app/services/theme.service.ts`
  - `/Users/ky/Workspace/rest-app-generator/src/ux/src/app/services/component-theme.service.ts`

### Scope
- Apply this rule across all UX files (components, shared styles, modal styling, graph styling, etc.).
- Keep theming reusable and consistent across screens.

### Request Loading UX (Mandatory)
- Use `LoadingOverlayComponent` for request-in-progress/loading-blocker experiences across the application.
- Do not use `InprogressComponent` for request/loading states.
- Reserve `InprogressComponent` only for dedicated informational/work-in-progress pages, not API/request progress.

### Root Cause First (Mandatory)
- Do not apply band-aid or speculative fixes just to mask a problem.
- Fix issues only after identifying a defensible root cause from code, runtime behavior, logs, or reproducible evidence.
- If the root cause is not yet found, do not ship a workaround as if it were the fix.
- In that case, clearly inform the user that the root cause has not been confirmed yet, summarize what was checked, and state the next targeted debugging step.

### Confirmation Toggle State (Mandatory)
- For any destructive checkbox toggle that needs confirmation, do not let UI commit unchecked state before user confirms.
- With Angular Material checkbox, prefer `(change)` and pass full `MatCheckboxChange` event.
- Keep previous state visually checked until confirmation result.
- On `Continue`, commit unchecked state and run cleanup.
- On `Cancel`, explicitly restore checked state on both bound model and checkbox source (`event.source.checked = true`), then trigger change detection.
- Apply this pattern across desktop and mobile to avoid visual desync.

### Flyway Migration Verification (Mandatory)
- For any backend/database change that adds, removes, renames, or edits Flyway migrations or schema-affecting JPA entities, run Flyway migrate after the code change.
- Use the app module Maven invocation so the project Flyway configuration and migration location are used:
  - `mvn -f pom.xml flyway:migrate`
- If datasource credentials are required, provide them via `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- Do not claim DB changes are complete until Flyway migrate has been run successfully or an explicit blocker has been reported.

### Cross-Module Flyway Packaging (Mandatory)
- If a Flyway migration lives outside `api/src/main/resources/rest-app-db/migration` but must be applied by the main app at runtime, make sure the main packaged app jar exposes that migration under `BOOT-INF/classes/rest-app-db/migration`.
- Do not assume a migration is discoverable by Flyway just because it exists in another module or nested dependency jar.
- After adding or moving migrations across modules, verify the packaged artifact contents, for example:
  - `jar tf target/*.jar | grep 'BOOT-INF/classes/rest-app-db/migration/'`
- If the database already has applied versions that are "not resolved locally", fix the artifact packaging or restore the missing migration files before considering `flyway repair`.
