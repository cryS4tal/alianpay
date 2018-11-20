CREATE TABLE t_notify (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  url VARCHAR(256) NOT NULL
  COMMENT '第三方回调url',
  params VARCHAR(1024)
  COMMENT '回调参数',
  fail_count INT DEFAULT 0
  COMMENT '失败次数',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

