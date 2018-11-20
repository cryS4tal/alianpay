/* 手机登录认证 */
CREATE TABLE t_phone_auth (
  id BIGINT PRIMARY KEY
  COMMENT '=t_account.id',
  phone VARCHAR(64) UNIQUE
  COMMENT '手机号',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);