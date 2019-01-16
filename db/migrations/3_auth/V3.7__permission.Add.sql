-- 添加系统预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_role_permission, @pm_type_sys, "角色管理", "修改系统角色权限、添加角色及设置角色权限");
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_account , @pm_type_sys, "人员管理", "管理系统内管理人员并设置角色");
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_sys.manage_dept, @pm_type_sys, "组织机构管理", "组织架构设置、组织机构默认角色的权限设置、组织机构分组管理、设置组织机构管理员");

-- 添加部门预置权限
INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_dept.manage_role_permission , @pm_type_dept, "角色管理", "修改角色权限、添加角色及设置角色权限");

INSERT INTO t_permission (id, type, name, description)
VALUES (@pm_dept.manage_account , @pm_type_dept, "人员管理", "组织机构内管理人员并设置角色");


