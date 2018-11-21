/*
 * Create database
 */
/*
CREATE DATABASE base_dev DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_bin;
 */
DELIMITER //

-- 添加用户
-- 说明：添加用户需要同时添加用户到个人部门
CREATE PROCEDURE _addUser(_user_id BIGINT, _username VARCHAR(128), _nickname VARCHAR(128))
  BEGIN

    INSERT INTO t_account (id, username, nickname)
    VALUES (_user_id, _username, _nickname);

    INSERT INTO t_dept_account (dept_id, account_id, role_id)
    VALUES (@personal_dept_id, _user_id, @role_personal);

  END //
DELIMITER ;