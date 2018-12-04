/**
 * 钱包.
 */
CREATE TABLE t_wallet (
  id BIGINT PRIMARY KEY
  COMMENT '=t_account.id',
  total INTEGER  DEFAULT 0 COMMENT '总金额',
  recharge INTEGER  DEFAULT 0 COMMENT '充值金额',
  bouns INTEGER  DEFAULT 0 COMMENT '分润金额',
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
  type VARCHAR(64) COMMENT '操作类型',
  money INTEGER  DEFAULT NULL COMMENT '交易金额',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_wallet_log`
ADD COLUMN `admin_id`  bigint NULL AFTER `id`,
ADD COLUMN `current_money`  int NULL AFTER `money`;

ALTER TABLE `t_wallet`
MODIFY COLUMN `bonus`  double(16,2) NULL DEFAULT 0 COMMENT '分润金额' AFTER `recharge`;