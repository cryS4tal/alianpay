-- 预置系统管理员
INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_stats);

INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_channel);

INSERT INTO t_role_permission
(role_id, permission_id)
VALUES (@role_sys_manager, @pm_sys.manage_bank_payment);