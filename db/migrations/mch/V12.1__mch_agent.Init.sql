CREATE TABLE t_mch_agent (
  mch_id BIGINT PRIMARY KEY COMMENT 'account.id',
  mch_name VARCHAR(128) NOT NULL COMMENT '商户名称',
  link_phone VARCHAR(128) NOT NULL COMMENT '手机号',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);