-- ============================================================================
-- Development seed data (Slice 1) — demonstration accounts.
--
-- Password for EVERY seeded account: hse-demo-123 (BCrypt).
-- Real office emails are recorded in the `users` table by Account
-- Administration; account management itself is out of scope (PRD 2.2).
--
-- INSERT IGNORE keeps startup idempotent: existing rows are never modified.
-- ============================================================================

INSERT IGNORE INTO users (email, password_hash, role) VALUES
  ('manal12cf@gmail.com', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'ADMIN'),
  ('mahadwaqas28@gmail.com', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'MANAGER'),
  ('i253172@nu.edu.pk', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'INSPECTOR'),
  ('extra.manager@hse.local', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'MANAGER'),
  ('extra.inspector@hse.local', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'INSPECTOR');

-- Projects: one assigned to the demo Manager, one to the second Manager,
-- one deliberately unassigned (used to demonstrate project-scope denial).
INSERT IGNORE INTO projects (name) VALUES
  ('Head Office Fit-Out'),
  ('Warehouse Extension'),
  ('Site A Laboratory');

INSERT IGNORE INTO project_manager (project_id, manager_email)
SELECT p.id, 'mahadwaqas28@gmail.com' FROM projects p WHERE p.name = 'Head Office Fit-Out';

INSERT IGNORE INTO project_manager (project_id, manager_email)
SELECT p.id, 'extra.manager@hse.local' FROM projects p WHERE p.name = 'Warehouse Extension';
