-- ============================================================================
-- HSE Safety Reporting System — database schema baseline (Slice 1)
-- Authority: ADR-04 (relational database), architecture.md section 9 (data ownership)
--
-- Ownership:
--   users                -> User and Access Management
--   projects             -> Project Management
--   project_manager      -> Project Management (Manager project assignment, ADR-03)
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
-- Projects (Project Management). Full lifecycle is implemented in Slice 5;
-- the baseline table exists now because Manager project scope (ADR-03)
-- must be enforceable from Slice 1.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_projects_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Manager -> project assignment.
-- Primary key on project_id enforces at most one Manager per project at the
-- database level (every project has exactly one Manager; the application
-- ensures a Manager is assigned when a project is created, Slice 5).
-- The manager's email is matched against users.email.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS project_manager (
    project_id    BIGINT       NOT NULL,
    manager_email VARCHAR(255) NOT NULL,
    PRIMARY KEY (project_id),
    KEY idx_project_manager_email (manager_email),
    CONSTRAINT fk_project_manager_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
