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

### Modal Validation UX (Mandatory)
- Do not rely on native browser validation tooltips inside app modals or forms for required/invalid field handling.
- Prefer application-level validation with inline Material errors and toast notifications where the screen already uses the shared validator/toast pattern.
- Avoid native `required` on modal inputs when it causes browser popups like `Please fill in this field` to replace the app's own validation UX.
- For Angular Material modal form fields that show dynamic validation messages, prefer `subscriptSizing="dynamic"` so `mat-error` content is visible and does not collapse awkwardly.

### Root Cause First (Mandatory)
- Do not apply band-aid or speculative fixes just to mask a problem.
- Fix issues only after identifying a defensible root cause from code, runtime behavior, logs, or reproducible evidence.
- If the root cause is not yet found, do not ship a workaround as if it were the fix.
- In that case, clearly inform the user that the root cause has not been confirmed yet, summarize what was checked, and state the next targeted debugging step.

### Sensitive Logging (Mandatory)
- Do not log customer or user-sensitive data in plaintext.
- This includes OTPs, email addresses, phone numbers, access tokens, refresh tokens, captcha answers, and similar secrets or identifiers.
- If operational logging is required, log only non-sensitive metadata such as channel, outcome, or masked identifiers.

### Confirmation Toggle State (Mandatory)
- For any destructive checkbox toggle that needs confirmation, do not let UI commit unchecked state before user confirms.
- With Angular Material checkbox, prefer `(change)` and pass full `MatCheckboxChange` event.
- Keep previous state visually checked until confirmation result.
- On `Continue`, commit unchecked state and run cleanup.
- On `Cancel`, explicitly restore checked state on both bound model and checkbox source (`event.source.checked = true`), then trigger change detection.
- Apply this pattern across desktop and mobile to avoid visual desync.

### Project Generation Section Structure (Mandatory)
- Do not keep `activeSection` screen content inline inside the project-generation dashboard template when adding or refactoring sections.
- Each project-generation section must live in its own dedicated component with separate `.ts`, `.html`, and `.css` files under the same dashboard folder.
- Keep the parent dashboard focused on orchestration, navigation, and shared state, and keep section rendering modular.

### Flyway Migration Verification (Mandatory)
- For any backend/database change that adds, removes, renames, or edits Flyway migrations or schema-affecting JPA entities, run Flyway migrate after the code change.
- The only canonical source location for app Flyway SQL migrations is:
  - `/Users/ky/Workspace/rest-app-generator/api/src/main/resources/rest-app-db/migration`
- Do not add new app migrations under any other module or resource path.
- Use the app module Maven invocation so the project Flyway configuration and migration location are used:
  - `mvn -f pom.xml flyway:migrate`
- If datasource credentials are required, provide them via `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- Do not claim DB changes are complete until Flyway migrate has been run successfully or an explicit blocker has been reported.

### Flyway Packaging (Mandatory)
- Runtime Flyway and Maven Flyway must both resolve migrations from the same canonical app path:
  - `classpath:rest-app-db/migration`
  - `filesystem:api/src/main/resources/rest-app-db/migration`
- Do not reintroduce extra migration locations in module poms or plugin configuration.
- After changing Flyway packaging or moving migrations, verify the runtime classes contain the full set under:
  - `target/classes/rest-app-db/migration`
- If the database already has applied versions that are "not resolved locally", restore the missing migration files or correct the configured migration location before considering `flyway repair`.

### Backend Route Changes (Mandatory)
- For any backend change that adds, removes, or changes controller mappings, endpoint methods, WebSocket endpoints, filters, or security routing, assume a backend restart is required before runtime verification.
- Do not describe a new API route as available unless the code has been compiled and the response clearly notes that the running backend must be restarted, or you have already verified it on a restarted server.
- When a frontend change depends on a new backend route, explicitly call out the restart requirement in the final response to avoid stale-server false alarms such as `HttpRequestMethodNotSupportedException`.

### DB-Driven Feature Access (Mandatory)
- Do not hardcode role-to-feature or role-to-settings visibility mappings in backend or UX code.
- Feature visibility and management access must come from DB-backed permissions and configuration only.
- Prefer authority-based checks such as `hasAuthority(...)` on the backend and authenticated permission arrays on the frontend.
- If a role such as `ROLE_SUPER_ADMIN` needs access, grant it through DB migrations or seeded role-permission mappings, not by hardcoding role checks in controllers or components.
