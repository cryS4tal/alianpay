-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_user_wallet, @pm_type_sys, "钱包管理", "给用户充值");