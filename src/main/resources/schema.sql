-- ============================================================================
-- HSE Safety Reporting System — database schema baseline (Slice 1)
-- Authority: ADR-04 (relational database), architecture.md section 9 (data ownership)
--
-- Ownership:
--   users                -> User and Access Management
--   projects             -> Project Management (incl. manager_email: the single
--                           Manager assignment, ADR-03 / FR-31)
--
-- Executed by Spring Boot on every start (spring.sql.init.mode=always);
-- must stay idempotent. JPA validates entities against this file (ddl-auto=validate).
-- Later slices extend this file; do not let Hibernate generate the schema.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Users, roles and credentials (NFR-01: login with company email and password).
-- Account creation, password reset and lockout are outside the software
-- (PRD section 2.2); rows are provisioned by Account Administration.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Projects (Project Management).
--
-- manager_email holds the ONE Manager assigned to the project (FR-31, EC-10):
-- NULL until an Admin assigns one; matched against users.email. Exactly one
-- Manager at a time is enforced by the single column — a second Manager can
-- only be set by overwriting the first (reassignment, FR-45).
--
-- state   OPEN | CLOSED  — Admin closes a project (FR-32…FR-35).
-- closed_at — exact closure timestamp; starts the 3-month retention window
--             for purge-all (NFR-03, FR-39).
-- Full lifecycle (reports on close) is implemented in Slice 5; the columns
-- exist now because JPA validates the Project entity against this file.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200) NOT NULL,
    manager_email VARCHAR(200) NULL,
    state         VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    closed_at     DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_projects_name (name),
    KEY idx_projects_manager_email (manager_email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Slice 3 additions (SOR creation & Photo Evidence).
-- The shared `users` and `projects` tables above remain owned by Slice 1/6.
-- ---------------------------------------------------------------------------

-- FR-04: the Inspector's company Manager designation (not login/role data).
CREATE TABLE IF NOT EXISTS inspector_designation (
    inspector_email VARCHAR(255) NOT NULL,
    manager_email   VARCHAR(255) NULL,
    PRIMARY KEY (inspector_email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- SOR-owned observer profile: auto-fills the SOR header from the signed-in Inspector.
CREATE TABLE IF NOT EXISTS observer_profile (
    email       VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255) NOT NULL,
    badge_no    VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    company     VARCHAR(255) NOT NULL,
    PRIMARY KEY (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS safety_report (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    project_id        BIGINT       NOT NULL,
    inspector_email   VARCHAR(255) NOT NULL,
    observer_name     VARCHAR(255) NOT NULL,
    observer_badge_no VARCHAR(255) NOT NULL,
    observer_title    VARCHAR(255) NOT NULL,
    observer_company  VARCHAR(255) NOT NULL,
    manager_email     VARCHAR(255) NOT NULL,
    observation_date  DATE         NOT NULL,
    observation_time  TIME         NOT NULL,
    location          VARCHAR(255) NOT NULL,
    observation_type  VARCHAR(255) NOT NULL,
    submission_status VARCHAR(255) NOT NULL,
    progress_status   VARCHAR(255) NULL,
    -- FR-11: stored with the report when a Manager rejects it. Displayed only
    -- while submission_status = REJECTED; retained after resubmission (FR-43).
    rejection_reason  VARCHAR(2000) NULL,
    deleted           BIT(1)       NOT NULL DEFAULT b'0',
    submitted_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_safety_report_project (project_id),
    KEY idx_safety_report_inspector (inspector_email),
    KEY idx_safety_report_manager (manager_email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS safety_violation (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    report_id               BIGINT        NOT NULL,
    brief_description       VARCHAR(2000) NOT NULL,
    observation_hazard      VARCHAR(255)  NOT NULL,
    observation_hazard_other VARCHAR(255)  NULL,
    risk_rating             VARCHAR(255)  NOT NULL,
    risk_rating_other       VARCHAR(255)  NULL,
    action_recommended      VARCHAR(2000) NULL,
    violator_name           VARCHAR(255)  NOT NULL,
    violator_badge_no       VARCHAR(255)  NOT NULL,
    violator_title          VARCHAR(255)  NOT NULL,
    task_incharge           VARCHAR(255)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_violation_report (report_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS report_version (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    report_id         BIGINT       NOT NULL,
    version_number    INT          NOT NULL,
    submitted_at      DATETIME(6)  NOT NULL,
    project_id        BIGINT       NOT NULL,
    inspector_email   VARCHAR(255) NOT NULL,
    observer_name     VARCHAR(255) NOT NULL,
    observer_badge_no VARCHAR(255) NOT NULL,
    observer_title    VARCHAR(255) NOT NULL,
    observer_company  VARCHAR(255) NOT NULL,
    manager_email     VARCHAR(255) NOT NULL,
    observation_date  DATE         NOT NULL,
    observation_time  TIME         NOT NULL,
    location          VARCHAR(255) NOT NULL,
    observation_type  VARCHAR(255) NOT NULL,
    violations_json   TEXT         NOT NULL,
    PRIMARY KEY (id),
    KEY idx_version_report (report_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS photograph_evidence (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    violation_id      BIGINT       NOT NULL,
    stored_file_name  VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NULL,
    content_type      VARCHAR(255) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_photo_file (stored_file_name),
    KEY idx_photo_violation (violation_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Slice 4: rejection notification (FR-42, FR-11, AC-12).
--
-- Created only when a Manager rejects a pending report, and delivered to that
-- report's Inspector alone — no other user receives it (FR-42). report_id is a
-- plain reference to safety_report; the displayed reference (RPT-%04d) is
-- derived in the view model, never stored here.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    report_id      BIGINT        NOT NULL,
    inspector_email VARCHAR(255) NOT NULL,
    reason         VARCHAR(2000) NOT NULL,
    created_at     DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notification_inspector (inspector_email),
    KEY idx_notification_report (report_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- EC-12: one-time submission token; a used token keeps its first report id.
CREATE TABLE IF NOT EXISTS submission_token (
    token      VARCHAR(255) NOT NULL,
    report_id  BIGINT       NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
