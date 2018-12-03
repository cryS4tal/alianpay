/**
 * 账单表（易付宝）.
 */
CREATE TABLE t_yfb_bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  order_no VARCHAR(64) COMMENT '平台订单号',
  sub_no VARCHAR(64) COMMENT '商户订单号',
  super_no VARCHAR(64) COMMENT '先锋订单号',
  amount INTEGER NOT NULL COMMENT '交易金额 /元',
  status TINYINT NOT NULL COMMENT '订单状态：new 1 / ing 2 / finish 3 / cancel 4',
  memo VARCHAR(256) COMMENT '商户系统保留域',
  notify_url VARCHAR(512) COMMENT '异步通知地址',
  redirect_url VARCHAR(512) COMMENT '前端跳转地址',
  trade_time DATETIME COMMENT '交易成立时间',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_yfb_bill`
ADD UNIQUE INDEX `u_order_no` (`order_no`) ,
ADD UNIQUE INDEX `u_sub_no` (`sub_no`) ,
ADD UNIQUE INDEX `u_supper_no` (`super_no`) ,
ADD INDEX `n_user_id` (`user_id`) ;

ALTER TABLE `t_yfb_bill`
ADD COLUMN `msg`  varchar(256) NULL AFTER `trade_time`,
ADD COLUMN `is_success`  tinyint(4) NULL AFTER `msg`;

ALTER TABLE `t_yfb_bill`
ADD COLUMN `pay_type`  varchar(32) NULL COMMENT '支付类型' AFTER `is_success`;

ALTER TABLE `t_yfb_bill`
ADD INDEX `n_status` (`status`) ;