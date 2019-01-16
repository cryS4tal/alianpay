CREATE TABLE t_wz_cash_log (
  id  BIGINT PRIMARY KEY AUTO_INCREMENT,
  log_id BIGINT COMMENT '=提现申请id',
  fail_count int(4) DEFAULT '0' COMMENT '失败次数',
  errcode VARCHAR(32)
  COMMENT '错误码',
  errmsg VARCHAR(256)
  COMMENT '错误msg'
);

CREATE TABLE t_sys_payment_log (
  id  BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id VARCHAR(64) COMMENT '退款单号',
  type VARCHAR(32) COMMENT '代付类型',
  fail_count int(4) DEFAULT '0' COMMENT '失败次数',
  errcode VARCHAR(32) COMMENT '错误码',
  errmsg VARCHAR(256) COMMENT '错误msg',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_sys_payment_log`
ADD UNIQUE INDEX `u_order_id` (`order_id`) ;

ALTER TABLE `t_cash_log`
ADD COLUMN `msg`  varchar(256) NULL COMMENT '描述' AFTER `reserved_phone`;