/**
 * 钱包.
 */
CREATE TABLE t_wallet (
  id BIGINT PRIMARY KEY
  COMMENT '=t_account.id',
  total INTEGER  DEFAULT 0 COMMENT '总金额',
  recharge INTEGER  DEFAULT 0 COMMENT '交易金额',
  pending INTEGER  DEFAULT 0 COMMENT '待处理金额',
  bonus INTEGER  DEFAULT 0 COMMENT '分润金额',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

CREATE TABLE t_trade_password (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  password VARCHAR(128) COMMENT '密码',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_trade_password`
ADD UNIQUE INDEX `u_user_id` (`user_id`) ;

CREATE TABLE t_wallet_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  admin_id BIGINT COMMENT '管理员id',
  type VARCHAR(64) COMMENT '操作类型',
  money INTEGER  DEFAULT NULL COMMENT '交易金额',
  current_money INTEGER DEFAULT NULL COMMENT '当前余额',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

CREATE TABLE t_cash_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  money INTEGER COMMENT '交易金额',
  open_bank VARCHAR(64) COMMENT '开户行',
  sub_bank VARCHAR(128) COMMENT '开户支行',
  bankcard_number VARCHAR(128) COMMENT '银行卡号',
  name VARCHAR(64) COMMENT '姓名',
  identity_card VARCHAR(32) COMMENT '身份证',
  reserved_phone VARCHAR(32) COMMENT '预留手机号',
  state INTEGER COMMENT '是否到账:0-待处理，1-成功，2-失败',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);