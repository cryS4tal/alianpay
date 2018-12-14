-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_stats, @pm_type_sys, "数据统计", "数据统计");