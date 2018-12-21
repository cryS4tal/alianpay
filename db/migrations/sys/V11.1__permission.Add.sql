-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_stats, @pm_type_sys, "数据统计", "数据统计");

INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_channel, @pm_type_sys, "通道管理", "切换系统通道");

INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_bank_payment, @pm_type_sys, "代付通道管理", "切换系统代付通道");