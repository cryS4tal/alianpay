/**
 * 账单表
 */
CREATE TABLE t_bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '商户id',
  sys_order_id VARCHAR(64) COMMENT '系统订单号',
  mch_order_id VARCHAR(64) COMMENT '商户订单号',
  super_order_id VARCHAR(64) COMMENT '通道订单号',
  app_id BIGINT COMMENT '应用id',
  channel_id BIGINT COMMENT '通道id',
  money INTEGER NOT NULL COMMENT '交易金额 单位分',
  status INTEGER NOT NULL COMMENT '订单状态：new 1 / ing 2 / finish 3 / cancel 4',
  reserve VARCHAR(256) COMMENT '商户系统保留域',
  notify_url VARCHAR(512) COMMENT '异步通知地址',
  redirect_url VARCHAR(512) COMMENT '前端跳转地址',
  is_success  tinyint(4) COMMENT '子系统是否接受通知',
  pay_type  varchar(32) COMMENT '支付类型',
  trade_type  varchar(32) COMMENT '支付方式',
  trade_time DATETIME COMMENT '交易成立时间',
  pay_charge  INTEGER COMMENT '手续费',
  msg varchar(256) COMMENT '易付宝支付返回金额',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_bill`
ADD UNIQUE INDEX `u_sys_order_id` (`sys_order_id`) ,
ADD UNIQUE INDEX `u_mch_order_id` (`mch_order_id`) ,
ADD UNIQUE INDEX `u_super_order_id` (`super_order_id`) ,
ADD INDEX `n_mch_id` (`mch_id`) ;

ALTER TABLE `t_bill`
ADD INDEX `n_status` (`status`) ;