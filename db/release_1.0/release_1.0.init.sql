/* 修改 t_account_password to t_password */
ALTER TABLE t_account_password RENAME TO t_password;

DROP TABLE IF EXISTS `t_user_base`;
CREATE TABLE t_user_base (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '商户id',
  mch_name VARCHAR(256) COMMENT '商户名称',
  nick_name VARCHAR(128) COMMENT '商户简称',
  email VARCHAR(64) COMMENT '联系人邮箱',
  link_name VARCHAR(64) COMMENT '联系人',
  link_phone VARCHAR(32) COMMENT '联系人手机',
  legal_name VARCHAR(64) COMMENT '法人',
  legal_phone VARCHAR(32) COMMENT '法人手机',
  org_code VARCHAR(128) COMMENT '组织机构代码',
  business_license VARCHAR(128) COMMENT '营业执照',
  state INTEGER DEFAULT 0 COMMENT '审核状态：0-new,1-pass,2-fail',
	card_images VARCHAR(1024) COMMENT '身份证照片',
  license_images VARCHAR(1024) COMMENT '营业执照照片',
  other_images VARCHAR(1024) COMMENT '其他照片',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_id` (`mch_id`),
  UNIQUE KEY `u_org_code` (`org_code`),
  UNIQUE KEY `u_business_license` (`business_license`),
  KEY `n_state` (`state`)
);

DROP TABLE IF EXISTS `t_sys_app`;
CREATE TABLE t_sys_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
  rate INTEGER  COMMENT '最低费率',
  status TINYINT NOT NULL COMMENT '0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

DROP TABLE IF EXISTS `t_mch_rate`;
CREATE TABLE t_mch_rate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  app_id BIGINT NOT NULL COMMENT '应用id',
  rate INTEGER  COMMENT '商户费率',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_app` (`mch_id`,`app_id`)
);

ALTER TABLE `t_wallet`
ADD COLUMN `pending`  INTEGER DEFAULT 0 AFTER `recharge`;

/* 删除历史数据，只对提现成功的进行保留 */
DELETE FROM t_cash_log WHERE is_ok = 0;

ALTER TABLE `t_cash_log`
CHANGE COLUMN `user_id` `mch_id`  bigint NULL DEFAULT NULL AFTER `id`,
CHANGE COLUMN `is_ok` `state`  integer NULL DEFAULT 0 COMMENT '是否到账:0-待处理，1-成功，2-失败' AFTER `money`,
ADD COLUMN `open_bank`  varchar(64) NULL COMMENT '开户行' AFTER `money`,
ADD COLUMN `sub_bank`  varchar(128) NULL COMMENT '开户支行' AFTER `open_bank`,
ADD COLUMN `bankcard_number`  varchar(128) NULL COMMENT '银行卡号' AFTER `sub_bank`,
ADD COLUMN `name`  varchar(64) NULL COMMENT '姓名' AFTER `bankcard_number`,
ADD COLUMN `identity_card`  varchar(32) NULL COMMENT '身份证' AFTER `name`,
ADD COLUMN `reserved_phone`  varchar(32) NULL COMMENT '预留手机号' AFTER `identity_card`;

CREATE TABLE t_sys_channel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) COMMENT '通道代码',
  name VARCHAR(128) COMMENT '通道名称',
  state TINYINT COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);
/* temp sql. 基于1.0 版本通道选择 */
INSERT INTO `t_sys_channel` VALUES ('1', 'YFB', '易付宝', '0', '2018-12-11 10:29:58', '2018-12-11 10:29:58');
INSERT INTO `t_sys_channel` VALUES ('2', 'WZ', '网众', '1', '2018-12-11 10:30:26', '2018-12-11 10:30:26');

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

INSERT INTO `t_sys_app` VALUES ('1', '支付宝H5', '300', '1', '2018-12-11 17:17:32', '2018-12-11 17:17:32');
INSERT INTO `t_sys_app` VALUES ('2', '微信H5', '300', '1', '2018-12-11 17:17:59', '2018-12-11 17:17:59');
INSERT INTO `t_sys_app` VALUES ('3', '支付宝扫码', '300', '1', '2018-12-11 17:18:29', '2018-12-11 17:18:29');
INSERT INTO `t_sys_app` VALUES ('4', '微信扫码', '300', '1', '2018-12-11 17:18:41', '2018-12-11 17:18:41');