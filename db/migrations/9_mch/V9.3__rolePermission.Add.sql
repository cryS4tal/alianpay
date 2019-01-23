-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_agency);

-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_rate);

-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_user_base);

-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_user_account);