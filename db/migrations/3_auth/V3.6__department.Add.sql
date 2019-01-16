
ALTER TABLE t_department MODIFY COLUMN id BIGINT;

-- 系统部门
INSERT INTO t_department
(id, type, name, description)
VALUES (@sys_dept_id, @dept_type_sys, '系统部门', '预设置唯一系统部门');

-- 个人部门
INSERT INTO t_department
(id, type, name, description)
VALUES (@personal_dept_id, @dept_type_personal,'个人', '系统预设个人群组');

ALTER TABLE t_department MODIFY COLUMN id BIGINT AUTO_INCREMENT;


