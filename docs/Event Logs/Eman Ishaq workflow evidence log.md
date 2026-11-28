# Workflow Evidence Log — Eman Ishaq

**Project:** HSE Safety Reporting System  
**Repository:** `ise_Final_Project`  
**Branch:** `eman`  
**Plan version:** v07
**Date:** 27 November 2026  

## Purpose

This log records the verification evidence for the merged Slice 3 report-creation flow after aligning it with the shared application theme and implementing the hand-drawn SOR layout, observer/manager auto-fill and configured observation hazard/risk lists.

## Workflow evidence

### 1. Merge and integration

- Merged `origin/main` (Slices 1/2/6, `com.hse.reporting`) into Eman's Slice 3 branch, producing merge commit `d9b2092`.
- Kept Slice 1 as the single application/security baseline: email login, shared `SecurityConfig`, `fragments.html`, `app.css` and role dashboards.
- Removed duplicate Slice 3 infrastructure (application class, security config, login controller, user entity/repository/role code).
- Moved FR-04 Inspector→Manager designation into `inspector_designation`; shared `users` remains owned by User and Access Management.
- Reused Project Management's shared `Project` entity and repository instead of a duplicate Slice 3 mapping.
- Added Slice 3 report/photo/designation tables to `schema.sql` and `schema-test.sql`; Hibernate remains on `ddl-auto=validate`.

### 2. UI consistency

- Rewrote `report/new.html` and `report/submitted.html` to use `fragments :: headWithReport`, shared `topbar`, `.container`, `.card`, `.alert`, `.primary` and `.hint`.
- Added `report-form.css` as a thin layer over `app.css`.
- Moved report creation from the shared topbar to an Inspector-dashboard **+ New report** button in `dashboard/inspector.html` (old navlink removed from `fragments.html`).
- Fixed the photograph field name to `violations[<index>].photos` so multipart binding works.

### 3. Hazard/severity dropdown revision (v06; superseded by §4)

- Added example values in `application.yml`:
  - Hazard categories: `Working at height`, `Slip/trip/fall`, `Manual handling`, `Electrical exposure`, `Fire/explosion`, `Chemical exposure`, `Struck by moving object`.
  - Severity levels: `Low`, `Medium`, `High`, `Critical`.
  - `OTHER` remains appended and is overridable via `HSE_HAZARD_CATEGORIES` / `HSE_SEVERITY_LEVELS`.
- The **Other hazard category** and **Other severity level** inputs are hidden, disabled and cleared until `OTHER` is selected. Selecting a listed value hides and clears them again.
- Verified the same behavior for a cloned violation row created by **Add another violation**.

### 4. SOR layout and data revision (v07)

- Replaced the Add Report page with the hand-drawn SOR sequence: SOR Details, Observer Details, Observation Details, repeated observation/violator blocks, Images and Submit.
- Added an SOR-owned `observer_profile` table keyed by email and seeded demo Inspector profiles; Name / Badge No / Title / Company are auto-filled from the signed-in account and snapshotted with the report.
- `ProjectEligibilityPort.Eligibility` now carries the selected project's `manager_email`; the form displays it and the domain/version snapshot persist it.
- Observation Date is required; Date, Time, Location and Type of Observation are report-level and mirrored read-only into every repeated observation block.
- Replaced Hazard Category / Severity Level with configured Observation Hazard / Risk Rating lists (`HSE_OBSERVATION_HAZARDS` / `HSE_RISK_RATINGS`), each with a conditional OTHER free-text input.
- Violator Name, Badge No, Title and Task Incharge are now required user-entered fields per observation; photo evidence remains attached per violation.
- Updated `schema.sql`, `schema-test.sql`, `data.sql`, `data-test.sql`, the domain entities, report version snapshot and report submission use case accordingly; `ddl-auto=validate` is unchanged.

### 5. Automated tests

Command:

```powershell
mvn clean test
```

Result:

```text
Tests run: 78, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 6. Browser verification

App URL: <http://localhost:8080/login>

Checks performed in the in-app browser against a fresh `hse_sor_check` database:

1. Signed in as `inspector@hse.test` / `password`.
2. Inspector dashboard rendered with the shared topbar and a **+ New report** button at the top-right; the button opened `/reports/new`.
3. `/reports/new` rendered the SOR Details, Observer Details, Observation Details, repeated observation/violator blocks, Images and Submit sections with the shared theme assets (`app.css`, `report-form.css`, `report-form.js`). No console errors or failed asset requests were observed.
4. Observer Details auto-filled `Eman Ishaq`, `SOR-0142`, `Safety Inspector` and the profile company. Selecting project `Sample Site` auto-filled `manager@hse.test`.
5. Observation Hazard and Risk Rating dropdowns showed the configured examples plus `OTHER`; both OTHER inputs started hidden/disabled. Selecting `OTHER` revealed and enabled its free-text input.
6. Added a second observation. Its Date, Time, Location and Type of Observation were read-only mirrors of the first row, and its OTHER inputs also started hidden/disabled.
7. Submitted with listed values (`Unsafe condition`, `High`) and reached the styled **Report submitted** page (`Report 1`, `Pending Approval`) with no console errors.
8. Earlier checks (before the SOR revision) submitted a PNG photograph and replayed the same token to confirm the duplicate-submission warning without creating another report; Admin dashboard and `/projects` were confirmed to use the same theme.

### 7. Database checks

- `schema.sql` and `data.sql` applied successfully to an empty MySQL database (`hse_sor_check`), including `observer_profile` and the SOR report/violation/version columns.
- The browser submission reached `Report 1` in `Pending Approval` against that database; the submission use case persists the observer/Manager snapshots and selected observation hazard/risk rating through the validated SOR columns.
- Earlier checks applied `schema.sql` to `hse_schema_check` and verified report/photo/designation counts; those temporary checks precede the SOR revision.
- Stale `application.properties` files in `target/classes` were removed so `application.yml` is the single runtime config source.

### 8. Login visibility and topbar role cleanup (v07 refinement)

- Added an accessible eye button to the login password field; the field starts hidden, the first click reveals the entered text, and the second click hides it again. The button uses a non-submitting `type="button"` and updates its label and `aria-pressed` state.
- Removed the duplicate plain-text role links from the shared topbar. The green role badge is now the single role label and is clickable: `INSPECTOR`, `MANAGER` and `ADMIN` each return to the role dashboard.
- Browser checks against `hse_sor_view` confirmed the reveal/hide cycle, one `INSPECTOR` badge in the Inspector header with no plain dashboard link, badge navigation back from `/reports/new`, and matching single-badge headers for Admin and Manager. No console errors were observed.

## Runtime note

MySQL and Spring Boot are running against the temporary `hse_sor_view` database so the updated pages can be previewed. The browser was left on the styled **Sign in** page.
