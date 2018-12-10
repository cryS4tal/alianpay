CREATE TABLE t_user_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  app_id VARCHAR(128) NOT NULL COMMENT '应用id',
  rate INTEGER  COMMENT '商户费率',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

CREATE TABLE t_sys_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
  rate INTEGER  COMMENT '最低费率',
  status TINYINT NOT NULL COMMENT '0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);