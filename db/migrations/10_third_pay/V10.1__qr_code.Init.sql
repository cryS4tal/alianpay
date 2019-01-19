CREATE TABLE t_qr_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  auth_id BIGINT COMMENT '用户id',
  code_url VARCHAR(64) COMMENT '收款码链接',
  uid VARCHAR(64) COMMENT 'aliUid',
  enable  tinyint(4) NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_code_url` (`code_url`),
  UNIQUE KEY `u_uid` (`uid`)
);

CREATE TABLE t_qr_pend_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) COMMENT '昵称',
  money INTEGER NULL COMMENT '交易金额 单位分',
  order_time DATETIME NULL,
  enable  tinyint(4) NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);