# Workflow Evidence Log — Eman Ishaq

**Project:** HSE Safety Reporting System  
**Repository:** `ise_Final_Project`  
**Branch:** `eman`  
**Plan version:** v06  
**Date:** 27 November 2026  

## Purpose

This log records the verification evidence for the merged Slice 3 report-creation flow after aligning it with the shared application theme and adding configurable hazard/severity choices.

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
- Added the INSPECTOR-only **New report** navlink to `fragments.html`.
- Fixed the photograph field name to `violations[<index>].photos` so multipart binding works.

### 3. Hazard/severity dropdown revision (v06)

- Added example values in `application.yml`:
  - Hazard categories: `Working at height`, `Slip/trip/fall`, `Manual handling`, `Electrical exposure`, `Fire/explosion`, `Chemical exposure`, `Struck by moving object`.
  - Severity levels: `Low`, `Medium`, `High`, `Critical`.
  - `OTHER` remains appended and is overridable via `HSE_HAZARD_CATEGORIES` / `HSE_SEVERITY_LEVELS`.
- The **Other hazard category** and **Other severity level** inputs are hidden, disabled and cleared until `OTHER` is selected. Selecting a listed value hides and clears them again.
- Verified the same behavior for a cloned violation row created by **Add another violation**.

### 4. Automated tests

Command:

```powershell
mvn clean test
```

Result:

```text
Tests run: 70, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 5. Browser verification

App URL: <http://localhost:8080/login>

Checks performed in the in-app browser:

1. Signed in as `inspector@hse.test` / `password`.
2. Inspector dashboard rendered with the shared topbar and **New report** link.
3. `/reports/new` loaded the shared theme assets (`app.css`, `report-form.css`, `report-form.js`) with no console errors or failed asset requests.
4. Dropdowns showed the configured examples plus `OTHER`; both OTHER inputs started hidden/disabled.
5. Selecting `OTHER` revealed and enabled both OTHER inputs; selecting a listed value hid and cleared them.
6. Added a second violation and confirmed its OTHER inputs also start hidden/disabled.
7. Submitted with listed values (`Working at height`, `High`) and reached the styled **Report submitted** page (`Report 3`, `Pending Approval`).
8. Earlier in the flow, submitted with a PNG photograph (`Report 2`) and replayed the same token to confirm the duplicate-submission warning without creating another report.
9. Signed in as `manal12cf@gmail.com` / `hse-demo-123` and confirmed Admin dashboard and `/projects` still use the same theme.

### 6. Database checks

- `schema.sql` applied successfully to an empty MySQL database (`hse_schema_check`), then the temporary database was removed.
- Runtime database counts after the browser flow: 1 report, 1 photograph, 3 Inspector designations for the first flow; a later listed-value submission created report 3.
- Stale `application.properties` files in `target/classes` were removed so `application.yml` is the single runtime config source.

## Runtime note

MySQL and Spring Boot were started as detached processes to avoid shell timeout termination. The browser tab was left on the styled **New report** form for preview.
