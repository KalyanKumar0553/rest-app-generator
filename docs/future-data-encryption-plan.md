# Future Data Encryption Plan

## Purpose

This note records the data-encryption feature work that was attempted, why it was reverted, and how to reintroduce the feature safely in the future.

The goal is to support:

- table-level encryption rules
- field-level encryption rules
- one-to-many shadow columns per encrypted field
- admin configuration from the Settings page
- safe lookup support without breaking existing application behavior

## What Was Reverted

The following feature set was removed after causing runtime instability in authentication and session/flush behavior:

- global Hibernate interceptor-based encryption/decryption
- admin API for managing `data_encryption_rules`
- settings UI for encryption-rule management
- `users.identifier_hash` lookup path
- one-to-many `data_encryption_rule_shadow_columns` model

## Root Cause Of Revert

The main issue was the runtime design, not the idea of encryption itself.

The reverted implementation attached a Hibernate interceptor globally to the custom entity manager. That meant normal auth-domain entities such as `User` were being mutated during Hibernate lifecycle callbacks (`onLoad`, `onSave`, `onFlushDirty`).

This led to auth/session instability, including:

- circular bean wiring pressure during startup
- risky flush-time mutations in hot paths
- `org.hibernate.AssertionFailure: collection owner not associated with session: com.src.main.auth.model.User.otps`

The system became too fragile for a cross-cutting interceptor approach.

## Important Lesson

If encryption is reintroduced, do **not** use a global Hibernate entity interceptor for auth and application entities.

Future implementation should prefer:

- explicit service-level encryption
- repository/query-level lookup strategy
- narrow targeting of sensitive fields only
- no automatic mutation of unrelated entities during flush/load

## Previously Introduced Schema Elements

These were created or planned and then cleaned up:

- `data_encryption_rules`
- `data_encryption_rule_shadow_columns`
- `users.identifier_hash`
- permissions:
  - `config.encryption.read`
  - `config.encryption.manage`
- admin route:
  - `/api/admin/data-encryption-rules/**`

## Cleanup Migration History

These migrations were part of the encryption-rule feature history:

- `V120__data_encryption_rules.sql`
- `V121__data_encryption_admin_and_hash_shadow.sql`
- `V134__admin_user_encryption_rules.sql`
- `V136__data_encryption_rule_shadow_columns.sql`
- `V137__remove_data_encryption_rule_feature.sql`

If this feature is revisited later, do not edit old applied migrations. Add only new forward migrations.

## Safe Future Architecture

### 1. Rule Registry

Keep DB-driven configuration, but use it only as metadata:

- `data_encryption_rules`
  - `id`
  - `table_name`
  - `column_name`
  - `enabled`
  - optional mode/scope fields if needed later

- `data_encryption_rule_shadow_columns`
  - `id`
  - `rule_id`
  - `shadow_column`
  - optional `shadow_type`
  - optional `normalizer`

This child-table model is still the correct direction if multiple shadow columns are needed.

### 2. Encryption Execution Model

Do not encrypt through Hibernate interception.

Instead:

- encrypt explicitly in service methods before saving sensitive values
- decrypt explicitly when returning values to callers
- compute shadow-column hashes explicitly in the same service method
- keep ORM persistence passive

This avoids flush-time side effects and session corruption.

### 3. Query Strategy

For searchable encrypted fields:

- store ciphertext in the primary sensitive column
- store deterministic lookup hashes in shadow columns
- query shadow columns for exact-match lookup
- never depend on SQL-side decryption for regular app flows

### 4. Scope The Feature Carefully

Do not start with `users.identifier`.

That field sits directly in:

- signup
- login
- OTP flow
- OAuth upsert
- user search
- project contributor resolution
- profile APIs

It is too central for the first rollout.

Start with lower-risk data, for example:

- optional profile PII
- newsletter email if business accepts explicit encryption there
- non-auth operational customer metadata

Only move into auth identifiers after proving the design in a narrower domain.

### 5. Admin UX

If the feature returns:

- super admin should manage rules from Settings
- UI should support:
  - table name
  - column name
  - enabled flag
  - add/remove multiple shadow columns
- validations should reject:
  - duplicate shadow column names
  - blank table name
  - using the encrypted source column also as a shadow column

### 6. Operational Requirements

Any future rollout should include:

- key management strategy
- rotation story
- backfill job for legacy plaintext data
- rollback strategy
- performance testing
- auth regression testing
- migration verification on a real DB before release

## Recommended Future Delivery Plan

### Phase 1

Design-only spike:

- identify target tables/fields
- classify which fields really need encryption
- define shadow-column semantics
- define key-management and rotation assumptions

### Phase 2

Low-risk pilot:

- implement service-level encryption for one low-risk table
- add explicit repository lookup using shadow columns
- avoid auth/user core flows

### Phase 3

Admin configuration:

- add DB-backed rule management
- add Settings UI
- add validation and audit logging

### Phase 4

Backfill and rollout:

- migrate old plaintext rows
- verify reads, writes, search, exports, and reporting
- test performance under realistic load

### Phase 5

Auth-adjacent fields only if still needed:

- introduce carefully
- isolate from login/session hot paths
- ship only with targeted regression coverage

## Non-Negotiable Rules For Future Reintroduction

- No global Hibernate interceptor
- No flush-time entity mutation for encryption
- No editing old Flyway migrations
- No production rollout before DB-backed migration verification
- No first rollout on `users.identifier`

## Suggested First Future Task

When this feature is revisited, start with a short design task:

1. pick one non-auth table and one field
2. define whether lookup needs one or many shadow columns
3. implement service-level encryption only for that field
4. verify read/write/search behavior
5. only then generalize into a DB-configured framework
