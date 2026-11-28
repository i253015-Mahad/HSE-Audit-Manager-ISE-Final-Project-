-- H2-compatible development seed for integration tests.
-- Password for every account: hse-demo-123 (BCrypt) — same as src/main/resources/data.sql.

INSERT INTO users (email, password_hash, role) VALUES
  ('manal12cf@gmail.com', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'ADMIN'),
  ('mahadwaqas28@gmail.com', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'MANAGER'),
  ('i253172@nu.edu.pk', '$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO', 'INSPECTOR');

INSERT INTO projects (name, manager_email) VALUES
  ('Head Office Fit-Out', 'mahadwaqas28@gmail.com'),
  ('Warehouse Extension', 'extra.manager@hse.local'),
  ('Site A Laboratory',   NULL);

INSERT INTO inspector_designation (inspector_email, manager_email) VALUES
  ('i253172@nu.edu.pk', 'mahadwaqas28@gmail.com');

INSERT INTO observer_profile (email, full_name, badge_no, title, company) VALUES
  ('i253172@nu.edu.pk', 'Inspector I253172', 'I-253172', 'Safety Inspector', 'HSE Department');
