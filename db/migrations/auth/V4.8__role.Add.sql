-- 添加系统预设的角色
ALTER TABLE t_role MODIFY COLUMN id BIGINT;

-- '@unlimit_role_dept_id-系统预设角色 1-系统角色 2-部门预设角色 3-部门角色',
-- 预设角色不关联任何部门
-- 系统预设角色
INSERT INTO t_role
(id, type, name, description, dept_id)
VALUES (@role_sys_manager, @role_type_sys_pre,
        '系统管理员', '预设系统管理员角色', @unlimit_role_dept_id);

-- 个人预设角色
INSERT INTO t_role
(id, type, name, description, dept_id)
VALUES (@role_personal, @role_type_dept_pre,
        '个人', '预设个人角色', @personal_dept_id);


-- 部门预设角色
INSERT INTO t_role
(id, type, name, description, dept_id)
VALUES (@role_dept_manager, @role_type_dept_pre,
        '管理员', '预设部门管理员角色', @unlimit_role_dept_id);

ALTER TABLE t_role MODIFY COLUMN id BIGINT AUTO_INCREMENT;
