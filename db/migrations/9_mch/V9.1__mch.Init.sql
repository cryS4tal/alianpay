CREATE TABLE t_user_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  app_id BIGINT NOT NULL COMMENT '应用id',
  rate INTEGER  COMMENT '商户费率',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_app` (`mch_id`,`app_id`)
);

CREATE TABLE t_sys_app (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
  rate INTEGER  COMMENT '最低费率',
  status TINYINT NOT NULL COMMENT '0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

INSERT INTO `t_sys_app` VALUES ('1', '支付宝H5', '300', '1', '2018-12-11 17:17:32', '2018-12-11 17:17:32');
INSERT INTO `t_sys_app` VALUES ('2', '微信H5', '300', '1', '2018-12-11 17:17:59', '2018-12-11 17:17:59');
INSERT INTO `t_sys_app` VALUES ('3', '支付宝扫码', '300', '1', '2018-12-11 17:18:29', '2018-12-11 17:18:29');
INSERT INTO `t_sys_app` VALUES ('4', '微信扫码', '300', '1', '2018-12-11 17:18:41', '2018-12-11 17:18:41');

CREATE TABLE t_user_key (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户id',
  secret_key VARCHAR(128) NOT NULL
  COMMENT '商家私钥',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

ALTER TABLE `t_user_key`
ADD UNIQUE INDEX `u_user_id` (`user_id`) ;

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

CREATE TABLE t_mch_sub (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mch_id BIGINT COMMENT '用户id',
  sub_id BIGINT NOT NULL COMMENT '子账户id',
  type INTEGER NOT NULL COMMENT '代理商类型：1-支付，2-代付',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now(),
  UNIQUE KEY `u_mch_app` (`mch_id`,`sub_id`)
);