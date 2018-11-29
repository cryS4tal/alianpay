-- 取消自增
ALTER TABLE t_account MODIFY COLUMN id BIGINT;
-- 添加系统管理员
-- 1 系统管理员
SET @user_id = 1001;
CALL _addUser(@user_id, 'test_system_manager', '测试-系统管理员');

INSERT INTO t_oauth2_uaa (id, open_id, access_token, expired_time, refresh_token)
VALUES (@user_id, @user_id, 'access_token', now(), 'refresh_token');

INSERT INTO t_dept_account (dept_id, account_id, role_id)
VALUES (@sys_dept_id, @user_id, @role_sys_manager);

-- 恢复自增
ALTER TABLE t_account MODIFY COLUMN id BIGINT AUTO_INCREMENT;

