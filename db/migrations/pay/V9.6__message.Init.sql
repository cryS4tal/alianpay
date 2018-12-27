CREATE TABLE t_async_message (
  id  BIGINT PRIMARY KEY AUTO_INCREMENT,
  bill_id  BIGINT,
  url VARCHAR(128) COMMENT '通知地址',
  params VARCHAR(1024) COMMENT '消息内容',
  fail_count int(4) DEFAULT '0' COMMENT '失败次数',
  errcode VARCHAR(32) COMMENT '错误码',
  errmsg VARCHAR(256) COMMENT '错误msg',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_async_message`
ADD UNIQUE INDEX `u_bill_id` (`bill_id`) ;

ALTER TABLE `t_async_message`
ADD COLUMN `bank_pay_order_id`  varchar(64) NULL AFTER `bill_id`;

ALTER TABLE `t_async_message`
ADD UNIQUE INDEX `u_order_id` (`bank_pay_order_id`) ;