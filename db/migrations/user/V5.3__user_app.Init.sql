CREATE TABLE t_user_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  app_id VARCHAR(128) NOT NULL
  COMMENT '应用id',
  app_name VARCHAR(128) NOT NULL
  COMMENT '应用名称',
  status TINYINT NOT NULL
  COMMENT '0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);