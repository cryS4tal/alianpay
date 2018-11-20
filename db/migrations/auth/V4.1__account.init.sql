CREATE TABLE t_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(128) NOT NULL
  COMMENT '用户名',
  nickname VARCHAR(128) NOT NULL
  COMMENT '昵称',
  avatar VARCHAR(256)
  COMMENT '头像链接',
  state VARCHAR(32) NOT NULL DEFAULT 'enable'
  COMMENT '用户状态。启用：enable, 禁用：disable',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);