CREATE TABLE t_qr_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  auth_id BIGINT COMMENT '用户id',
  code_url VARCHAR(64) COMMENT '收款码链接',
  uid VARCHAR(64) COMMENT 'aliUid',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_code_url` (`code_url`)
);