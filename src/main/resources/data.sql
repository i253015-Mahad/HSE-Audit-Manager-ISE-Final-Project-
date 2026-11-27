-- DEVELOPMENT SEED ONLY (profile "dev"). Replace with company designation data.
-- All three passwords are "password" (BCrypt). Roles: INSPECTOR, MANAGER, ADMIN.
-- manager_email on an INSPECTOR is the company designation used by FR-04.
INSERT INTO app_user (email, password_hash, role, manager_email, enabled)
SELECT 'inspector@hse.test', '$2a$12$CCH7lzifDcAvsEcL./Etv.oA/3Jq8llz2qbEE73JvSTqHRyUxfwKS', 'INSPECTOR', 'manager@hse.test', TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'inspector@hse.test');

INSERT INTO app_user (email, password_hash, role, manager_email, enabled)
SELECT 'manager@hse.test', '$2a$12$CCH7lzifDcAvsEcL./Etv.oA/3Jq8llz2qbEE73JvSTqHRyUxfwKS', 'MANAGER', NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'manager@hse.test');

INSERT INTO app_user (email, password_hash, role, manager_email, enabled)
SELECT 'admin@hse.test', '$2a$12$CCH7lzifDcAvsEcL./Etv.oA/3Jq8llz2qbEE73JvSTqHRyUxfwKS', 'ADMIN', NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@hse.test');

INSERT INTO project (name, manager_email, closed)
SELECT 'Sample Site', 'manager@hse.test', FALSE
WHERE NOT EXISTS (SELECT 1 FROM project WHERE name = 'Sample Site');
