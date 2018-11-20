/* 密码登录认证 */
CREATE TABLE t_account_password (
  id BIGINT PRIMARY KEY
  COMMENT '=t_account.id',
  password VARCHAR(128)
  COMMENT '密码',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);