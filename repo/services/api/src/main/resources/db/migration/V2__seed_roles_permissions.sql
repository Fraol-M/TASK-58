-- =====================================================
-- V2: Seed roles and permissions
-- =====================================================

-- Roles
INSERT INTO t_role (code, name, description) VALUES
    ('REGULAR_USER',     'Regular User',     'Standard user with basic access to fitness and study modules'),
    ('OPERATIONS_STAFF', 'Operations Staff',  'Staff member with access to inbound, master data, and reporting'),
    ('ADMIN',            'Administrator',     'Full system administrator with all permissions');

-- Permissions: fitness module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('fitness:read',    'View Fitness Records',   'fitness', 'read'),
    ('fitness:write',   'Create/Update Fitness',  'fitness', 'write'),
    ('fitness:delete',  'Delete Fitness Records', 'fitness', 'delete');

-- Permissions: study module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('study:read',    'View Study Records',   'study', 'read'),
    ('study:write',   'Create/Update Study',  'study', 'write'),
    ('study:delete',  'Delete Study Records', 'study', 'delete');

-- Permissions: inbound module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('inbound:read',    'View Inbound Records',   'inbound', 'read'),
    ('inbound:write',   'Create/Update Inbound',  'inbound', 'write'),
    ('inbound:delete',  'Delete Inbound Records', 'inbound', 'delete');

-- Permissions: masterdata module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('masterdata:read',    'View Master Data',          'masterdata', 'read'),
    ('masterdata:write',   'Create/Update Master Data', 'masterdata', 'write'),
    ('masterdata:delete',  'Delete Master Data',        'masterdata', 'delete');

-- Permissions: notification module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('notification:read',    'View Notifications',          'notification', 'read'),
    ('notification:write',   'Create/Update Notifications', 'notification', 'write'),
    ('notification:delete',  'Delete Notifications',        'notification', 'delete');

-- Permissions: export module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('export:read',    'View Exports',     'export', 'read'),
    ('export:write',   'Create Exports',   'export', 'write'),
    ('export:delete',  'Delete Exports',   'export', 'delete');

-- Permissions: reporting module
INSERT INTO t_permission (code, name, resource, action) VALUES
    ('reporting:read',    'View Reports',     'reporting', 'read'),
    ('reporting:write',   'Create Reports',   'reporting', 'write'),
    ('reporting:delete',  'Delete Reports',   'reporting', 'delete');

-- -------------------------------------------------------
-- Role-Permission mappings
-- -------------------------------------------------------

-- REGULAR_USER: read/write on fitness and study, read notifications
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM t_role r, t_permission p
WHERE r.code = 'REGULAR_USER' AND p.code IN (
    'fitness:read', 'fitness:write',
    'study:read', 'study:write',
    'notification:read'
);

-- OPERATIONS_STAFF: all of REGULAR_USER plus inbound, masterdata, export, reporting
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM t_role r, t_permission p
WHERE r.code = 'OPERATIONS_STAFF' AND p.code IN (
    'fitness:read', 'fitness:write',
    'study:read', 'study:write',
    'inbound:read', 'inbound:write',
    'masterdata:read', 'masterdata:write',
    'notification:read', 'notification:write',
    'export:read', 'export:write',
    'reporting:read', 'reporting:write'
);

-- ADMIN: all permissions
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM t_role r, t_permission p
WHERE r.code = 'ADMIN';
