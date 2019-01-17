-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_qr_code);

INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.qr_code);