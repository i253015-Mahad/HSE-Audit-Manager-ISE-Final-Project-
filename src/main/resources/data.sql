-- ============================================================================
-- Development seed data — demonstration accounts.
--
-- Password for the Slice 1 demonstration accounts: hse-demo-123 (BCrypt).
-- Password for the Slice 3 report-creation accounts below: password (BCrypt).
-- Real office emails are recorded in the `users` table by Account
-- Administration; account management itself is out of scope (PRD 2.2).
--
-- INSERT IGNORE keeps startup idempotent: existing rows are never modified.
-- ============================================================================

-- TODO(Manal): DEMO ONLY — these accounts exist so the login slice can be
-- demonstrated end-to-end; they are not office accounts and must not ship
-- to a real deployment. Remove (or replace with provisioned rows) before go-live.
INSERT IGNORE INTO users (email, password_hash, role) VALUES
  ('manal12cf@gmail.com', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'ADMIN'),
  ('mahadwaqas28@gmail.com', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'MANAGER'),
  ('i253172@nu.edu.pk', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'INSPECTOR'),
  ('extra.manager@hse.local', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'MANAGER'),
  ('extra.inspector@hse.local', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'INSPECTOR');

-- Slice 3 development accounts (password: password). These let the report
-- creation flow be demonstrated after the slices are merged.
INSERT IGNORE INTO users (email, password_hash, role) VALUES
  ('inspector@hse.test', '$2a$12$jjMKys.Xklr55pU3UhuXee0rKgXEPoS6.b0yAwQK78sCSNL9EJvQW', 'INSPECTOR'),
  ('manager@hse.test', '$2a$12$jjMKys.Xklr55pU3UhuXee0rKgXEPoS6.b0yAwQK78sCSNL9EJvQW', 'MANAGER'),
  ('admin@hse.test', '$2a$12$jjMKys.Xklr55pU3UhuXee0rKgXEPoS6.b0yAwQK78sCSNL9EJvQW', 'ADMIN');

-- Projects: one assigned to the demo Manager, one to the second Manager,
-- one deliberately unassigned (used to demonstrate project-scope denial).
-- The ONE Manager per project lives in projects.manager_email (FR-31).
INSERT IGNORE INTO projects (name, manager_email) VALUES
  ('Head Office Fit-Out', 'mahadwaqas28@gmail.com'),
  ('Warehouse Extension', 'extra.manager@hse.local'),
  ('Site A Laboratory',   NULL),
  ('Sample Site',         'manager@hse.test');

-- FR-04: an Inspector may report only on projects handled by their Manager.
-- The designation is a separate slice-owned table so the shared `users` table
-- stays owned by User and Access Management (architecture.md section 9).
INSERT IGNORE INTO inspector_designation (inspector_email, manager_email) VALUES
  ('inspector@hse.test',   'manager@hse.test'),
  ('i253172@nu.edu.pk',    'mahadwaqas28@gmail.com'),
  ('extra.inspector@hse.local', 'extra.manager@hse.local');
