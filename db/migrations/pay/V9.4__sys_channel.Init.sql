/**
 * 支付通道表
 */
CREATE TABLE t_sys_channel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) COMMENT '通道代码',
  name VARCHAR(128) COMMENT '通道名称',
  state TINYINT COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);