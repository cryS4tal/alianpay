-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_bill, @pm_type_sys, "商户账单管理", "商户账单管理");