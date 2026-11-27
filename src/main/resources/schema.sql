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
-- Users, roles and credentials (NFR-01: login with company-issued email).
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
