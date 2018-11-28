/**
 * 账单表（先锋）.
 */
CREATE TABLE t_xf_bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  order_no VARCHAR(64) COMMENT '平台订单号',
  sub_no VARCHAR(64) COMMENT '商户订单号',
  super_no VARCHAR(64) COMMENT '先锋订单号',
  amount INTEGER NOT NULL COMMENT '交易金额 单位分',
  status TINYINT NOT NULL COMMENT '订单状态：new 1 / ing 2 / finish 3 / cancel 4',
  account_no VARCHAR(64)  NOT NULL COMMENT '银行卡号',
  account_name VARCHAR(128) NOT NULL COMMENT '持卡人',
  mobile_no VARCHAR(64) COMMENT '手机',
  bank_no VARCHAR(32) NOT NULL COMMENT '银行编码',
  user_type INTEGER NOT NULL COMMENT '用户类型：1：对私 2：对公',
  account_type INTEGER COMMENT '账户类型',
  memo VARCHAR(256) COMMENT '商户系统保留域',
  trade_time DATETIME COMMENT '交易成立时间',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_xf_bill`
ADD UNIQUE INDEX `u_order_no` (`order_no`) ,
ADD UNIQUE INDEX `u_sub_no` (`sub_no`) ,
ADD UNIQUE INDEX `u_supper_no` (`super_no`) ,
ADD INDEX `n_user_id` (`user_id`) ;

ALTER TABLE `t_xf_bill`
ADD COLUMN `res_code`  varchar(64) NULL COMMENT '先锋支付返回code' AFTER `trade_time`,
ADD COLUMN `res_message`  varchar(256) NULL COMMENT '先锋支付返回message' AFTER `res_code`;
