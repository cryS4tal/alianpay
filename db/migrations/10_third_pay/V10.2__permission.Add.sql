-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_qr_code, @pm_type_sys, "管理个人收款码", "管理个人收款码");
