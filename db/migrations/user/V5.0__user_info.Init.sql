CREATE TABLE t_user_settlement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  identity_card VARCHAR(64) NOT NULL
  COMMENT '身份证',
  name VARCHAR(128) NOT NULL
  COMMENT '姓名',
  bankcard_number VARCHAR(128) NOT NULL
  COMMENT '银行卡号',
  reserved_phone VARCHAR(32) NOT NULL
  COMMENT '预留手机号',
  bank_type VARCHAR(16) NULL
  COMMENT '银行卡类型',
  open_bank VARCHAR(256) NOT NULL
  COMMENT '开户行',
  sub_bank VARCHAR(512) NOT NULL
  COMMENT '开户支行',
  charge_type INT NULL
  COMMENT '提现类型：1-定额；2-百分比',
  charge_rate INT NULL
  COMMENT '提现费率：分/万分之n',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_user_settlement`
ADD UNIQUE INDEX `u_user_id` (`user_id`) ;

ALTER TABLE `t_user_settlement`
MODIFY COLUMN `open_bank`  varchar(256)  NULL COMMENT '开户行' AFTER `bank_type`;