# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack REST API project generator: Spring Boot 3.3.4 (Java 17) backend + Angular 20 SPA frontend, deployed as a single JAR to Azure. Users define entities, controllers, and modules via a web UI, then generate downloadable project scaffolding. Includes AI Labs for AI-assisted project planning.

## Build & Run Commands

### Backend (Maven)
```bash
# Build all backend modules (skip tests)
mvn -U -f parent/pom.xml -DskipTests \
  -pl com.src:common,com.src:communication,com.src:rbac,com.src:auth,com.src:state-machine,com.src:swagger \
  -am clean install

# Run Spring Boot app (port 8080 default)
mvn -f pom.xml -DskipTests clean process-resources compile spring-boot:run

# Full local startup (stops existing, builds modules, runs app)
./run.sh

# Run Flyway migrations (requires DB env vars)
mvn -f pom.xml flyway:migrate
```

### Frontend (Angular, from `src/ux/`)
```bash
npm start                  # Dev server (ng serve)
npm run build              # Production build
npm run build:spring-boot  # Build into target/classes/static for embedded deployment
npm test                   # Karma unit tests
npm run e2e                # End-to-end tests
```

## Architecture

### Backend Modules (Maven multi-module)
- **api/** — Main Spring Boot app: controllers, services, repositories, models, DTOs, WebSocket handlers, workflow engine. Entry point: `com.src.main.RestAppGeneratorApplication`
- **common/** — Shared utilities, Jackson format support (YAML, XML, CSV)
- **auth/** — JWT (JJWT), OAuth2, Google Auth
- **rbac/** — Role-Based Access Control, Spring Security
- **communication/** — Email (Azure Communication Services), SMS (Twilio)
- **subscription/** — Subscription management, Spring Cache (Caffeine)
- **state-machine/** — Spring State Machine, Spring Initializr integration, Mustache templating
- **swagger/** — SpringDoc OpenAPI 2.6.0

### Database
- PostgreSQL with Flyway migrations
- Canonical migration path: `api/src/main/resources/rest-app-db/migration/`
- Credentials via env vars: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

### Frontend (src/ux/)
- Angular 20 with standalone components (no NgModules)
- Angular Material 20 + Bootstrap 5.3 + Ionic Angular 8.7 (mobile)
- RxJS-based state management (service pattern, no NgRx)
- Hash-based routing with lazy-loaded feature modules

**Lazy-loaded modules:**
- `modules/auth/` — Login, signup, OAuth, OTP
- `modules/user/` — Dashboard, projects, AI Labs, settings, profile
- `modules/project-generation/` — Java and Node project generation dashboards

**Key services:** `services/toast.service.ts` (notifications), `services/ai-labs.service.ts` (AI generation), `services/project.service.ts` (CRUD), `services/theme.service.ts` + `services/component-theme.service.ts` (theming)

**HTTP interceptor** (`interceptors/http.interceptor.ts`): Token injection, 30s timeout, error categorization by status code, automatic 401 logout.

## Mandatory Conventions (from AGENTS.md)

1. **No hardcoded colors** — Use CSS variable tokens from `src/ux/src/app/styles/colors.css` and `dark-theme.css`. Add new tokens there if needed; never use hex/rgb literals in components.

2. **Loading UI** — Use `LoadingOverlayComponent` for request progress. `InprogressComponent` is reserved for informational pages only.

3. **Root cause first** — No band-aid fixes. If root cause is unknown, say so and document what was checked.

4. **No sensitive data in logs** — Never log OTPs, emails, phone numbers, tokens, or secrets in plaintext.

5. **Confirmation toggle pattern** — For destructive checkbox toggles: use `[ngModel]` + `(ngModelChange)`, keep checked state until user confirms via modal, restore on cancel with `ChangeDetectorRef.detectChanges()`. See SKILL.md for full pattern.

6. **Flyway migrations** — Only add migrations under `api/src/main/resources/rest-app-db/migration/`. Run `mvn -f pom.xml flyway:migrate` after schema changes. Both runtime and Maven must resolve from `classpath:rest-app-db/migration`.

7. **Backend route changes** — Require server restart. Explicitly call out restart requirement when frontend depends on new routes.

## CI/CD

GitHub Actions (`.github/workflows/main_spring-init.yml`): builds with Maven + Java 17 on push to `main`, verifies Flyway migrations are packaged in JAR, deploys to Azure Web App.
