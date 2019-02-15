-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_qr_code, @pm_type_sys, "管理个人收款码", "管理个人收款码");

INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.qr_code, @pm_type_sys, "个人收款码", "设置个人收款码");

INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_xf, @pm_type_sys, "先锋充值.查询", "管理先锋充值.查询");
