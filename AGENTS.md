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
