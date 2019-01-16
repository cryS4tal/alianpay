-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_role_permission);

INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_account);

INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_dept);

-- 预设个人角色

-- 预置部门管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_dept_manager, @pm_dept.manage_role_permission);

INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_dept_manager, @pm_dept.manage_account);


