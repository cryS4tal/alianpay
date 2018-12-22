ALTER TABLE `t_account`
ADD INDEX `n_state` (`state`) ;

CREATE TABLE t_bank_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) COMMENT '标识',
  name VARCHAR(128) COMMENT '代付渠道',
  state TINYINT COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

INSERT INTO `t_bank_payment` VALUES ('1', 'pingAn', '平安', '1', '2018-12-11 10:29:58', '2018-12-11 10:29:58');
INSERT INTO `t_bank_payment` VALUES ('2', 'xianFen', '先锋', '0', '2018-12-11 10:30:26', '2018-12-11 10:30:26');

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