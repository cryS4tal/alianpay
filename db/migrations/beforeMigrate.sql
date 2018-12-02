SET TIME_ZONE = '+00:00';

-- 部门类型
-- 普通部门
SET @dept_type_dept = 0;
-- 系统部门
SET @dept_type_sys = 1;
-- 个人部门
SET @dept_type_personal = 2;

-- 权限类型
-- 组织权限
SET @pm_type_dept = 0;
-- 系统权限
SET @pm_type_sys = 1;

-- 角色类型
-- 系统预设角色
SET @role_type_sys_pre = 0;
-- 系统角色
SET @role_type_sys = 1;
-- 部门预设角色
SET @role_type_dept_pre = 2;
-- 部门角色
SET @role_type_dept = 3;

DELIMITER //
CREATE FUNCTION _init(code INT)
  RETURNS INT
BEGIN
  SET @model_code = code;
  SET @pm_sys_base = @model_code * 10000 + @pm_type_sys * 1000;
  SET @pm_dept_base = @model_code * 10000 + @pm_type_dept * 1000;
  RETURN 0;
END//
DELIMITER ;

-- 配置参数定义
/**
 * auth
 */
-- 初始化
SELECT _init(4);

-- 默认超级管理员Id
SET @account_super_id = 1;
-- 默认系统部门Id
SET @sys_dept_id = 1;
-- 默认个人部门Id
SET @personal_dept_id = 2;

-- 未限定角色的部门Id
SET @unlimit_role_dept_id = 0;
-- 系统权限
-- 管理角色与权限
SET @pm_sys.manage_role_permission = @pm_sys_base + 1;
-- 管理账户
SET @pm_sys.manage_account = @pm_sys_base + 2;
-- 管理部门
SET @pm_sys.manage_dept = @pm_sys_base + 4;

-- 部门权限
-- 管理角色与权限
SET @pm_dept.manage_role_permission = @pm_dept_base + 1;
-- 管理账户
SET @pm_dept.manage_account = @pm_dept_base + 2;
-- 角色
-- 预设系统管理员
SET @role_sys_manager = 1;

-- 预设个人角色
SET @role_personal = 100;

-- 预设部门管理员
SET @role_dept_manager = 1001;

/**
 * user
 */
-- 初始化
SELECT _init(5);
-- 系统权限
-- 管理商户提现费率
SET @pm_sys.manage_user_charge = @pm_sys_base + 1;
SET @pm_sys.manage_user_app = @pm_sys_base + 2;
SET @pm_sys.manage_user_audit = @pm_sys_base + 3;

/**
 * pay
 */
-- 初始化
SELECT _init(9);
-- 系统权限
-- 管理商户账单
SET @pm_sys.manage_user_bill = @pm_sys_base + 1;