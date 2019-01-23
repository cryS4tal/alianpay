-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_agency, @pm_type_sys, "代理商管理", "管理代理商");

-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_app, @pm_type_sys, "商户应用管理", "管理商户应用");

-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_base, @pm_type_sys, "商户基础信息管理", "管理商户基础信息");

-- 系统用户管理权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_account, @pm_type_sys, "系统用户管理", "管理系统用户");