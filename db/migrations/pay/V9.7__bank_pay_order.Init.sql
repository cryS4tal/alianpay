/**
 * 代付表
 */
CREATE TABLE t_bank_pay_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '商户id',
  sys_order_id VARCHAR(64) COMMENT '系统订单号',
  mch_order_id VARCHAR(64) COMMENT '商户订单号',
  super_order_id VARCHAR(64) COMMENT '通道订单号',
  money INTEGER NOT NULL COMMENT '交易金额 单位分',
  acc_no VARCHAR(64) COMMENT '银行卡号',
  acc_name VARCHAR(64) COMMENT '姓名',
  pay_type  INTEGER COMMENT '代付类型：1-对公，2-对私',
  acc_type  INTEGER COMMENT '账户类型',
  issuer VARCHAR(128) COMMENT '银联号',
  mobile VARCHAR(32) COMMENT '手机',
  bank_name VARCHAR(128) COMMENT '银行名称',
  bank_no VARCHAR(32) COMMENT '银行编码',
  notify_url VARCHAR(512) COMMENT '异步通知地址',
  bank_payment_id BIGINT COMMENT '代付通道id',
  charge_type INTEGER COMMENT '结算类型',
  charge_money INTEGER COMMENT '结算金额',
  is_success  tinyint(4) COMMENT '子系统是否接受通知',
  status INTEGER NOT NULL COMMENT '订单状态：new 1 / ing 2 / finish 3 / cancel 4',
  trade_time DATETIME COMMENT '交易成立时间',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_bank_pay_order`
ADD UNIQUE INDEX `u_sys_order_id` (`sys_order_id`) ,
ADD UNIQUE INDEX `u_mch_order_id` (`mch_order_id`) ,
ADD UNIQUE INDEX `u_super_order_id` (`super_order_id`) ,
ADD INDEX `n_status` (`status`) ,
ADD INDEX `n_is_success` (`is_success`) ,
ADD INDEX `n_pay_type` (`pay_type`) ,
ADD INDEX `n_acc_type` (`acc_type`) ,
ADD INDEX `n_charge_type` (`charge_type`) ,
ADD INDEX `n_mch_id` (`mch_id`) ;