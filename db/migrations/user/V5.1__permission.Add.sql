-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_charge, @pm_type_sys, "商户提现费率管理", "设置商户提现费率");

-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_app, @pm_type_sys, "商户应用管理", "管理商户应用");

-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_audit, @pm_type_sys, "代付用户资格审核管理", "审核代付用户资格");